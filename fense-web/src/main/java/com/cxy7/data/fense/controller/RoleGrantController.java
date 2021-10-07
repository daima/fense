package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
@RequestMapping("/roleGrant")
public class RoleGrantController {
	private static final Logger logger = LoggerFactory.getLogger(RoleGrantController.class);
	@Autowired
	private RoleService roleService;

	@ResponseBody
	@RequestMapping(value = "/list")
	public JSONArray auth() {
		return roleService.listGrants();
	}

	@ResponseBody
	@RequestMapping(value = "/auth")
	public JSONObject auth(int page, int rows, String id) {
		if (!NumberUtils.isDigits(id)) {
			logger.warn("ID错误:{}", id);
			return new JSONObject();
		}
		if (page < 0 || rows < 0) {
			return new JSONObject();
		}
		PageRequest pageable = PageRequest.of(page - 1, rows);
		int roleId = Integer.parseInt(id);
		return roleService.getAuthenticatedUsers(pageable, roleId);
	}

	@ResponseBody
	@RequestMapping(value = "/grant")
	public boolean grant(String oper, String id, String role_id, String name) {
		boolean succ = false;
		if ("add".equals(oper)) {
			if (!NumberUtils.isDigits(name) || !NumberUtils.isDigits(role_id)) {
				logger.error("参数输入错误！role_id:{}, user_id:{}", role_id, name);
				return succ;
			}
			// TODO 检查名称是否被占用

			int userId = Integer.parseInt(name);
			int roleId = Integer.parseInt(role_id);
			roleService.grant(roleId, userId);

			succ = true;
		} else if ("del".equals(oper)){
			if (StringUtils.isAnyBlank(id)) {
				logger.error("ID不能为空！");
				return succ;
			}
			int roleId = Integer.parseInt(id.split("_")[0]);
			int userId = Integer.parseInt(id.split("_")[1]);
			roleService.revoke(roleId, userId);
			succ = true;
		}
		return succ;
	}

}
