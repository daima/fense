package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.model.Approval;
import com.cxy7.data.fense.model.Role;
import com.cxy7.data.fense.service.ApprovalService;
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

import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
@RequestMapping("/role")
public class RoleController {
	private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
	@Autowired
	private RoleService roleService;
	@Autowired
	private ApprovalService approvalService;

	@RequestMapping("/add")
	public boolean add(String name) {
		boolean succ = false;
		if (StringUtils.isAnyBlank(name)) {
			logger.error("名称输入错误！");
			return succ;
		}
		Role role = roleService.add(name);
		logger.error("添加成功！roleId:" + role.getId());
		succ = true;
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "list")
	public JSONObject list(int page, int rows) {
		if (page < 0 || rows < 0) {
			return new JSONObject();
		}
		PageRequest pageable = PageRequest.of(page - 1, rows);

		return roleService.list(pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/listForGrant")
	public String listForGrant() {
		return roleService.listForGrant();
	}

	@ResponseBody
	@RequestMapping(value = "/listByUserId")
	public String listByUserId(String id) {
		if (!NumberUtils.isDigits(id)) {
			logger.error("参数错误：{}", id);
			return null;
		}
		long approvalId = Long.parseLong(id);
		Optional<Approval> op = approvalService.get(approvalId);
		if (!op.isPresent()) {
			logger.error("未找到该审批单：{}", approvalId);
			return null;
		}

		int applicant = op.get().getApply().getApplicant();
		return roleService.listByUser(applicant);
	}

	@ResponseBody
	@RequestMapping(value = "/edit")
	public boolean edit(String oper, String id, String name) {
		boolean succ = false;
		if ("add".equals(oper)) {
			if (StringUtils.isBlank(name)) {
				logger.error("名称输入错误！");
				return succ;
			}
			succ = add(name);
		} else if ("edit".equals(oper)){
			if (StringUtils.isAnyBlank(id, name)) {
				logger.error("ID或名称输入错误！");
				return succ;
			}
			int roleId = Integer.parseInt(id);
			succ = roleService.edit(roleId, name);
		} else if ("del".equals(oper)){
			if (StringUtils.isBlank(id)) {
				logger.error("ID不能为空！");
				return succ;
			}
			int roleId = Integer.parseInt(id);
			succ = delete(roleId);
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/delete")
	public boolean delete(int id) {
		logger.info("正在删除角色:{}", id);
		boolean succ = roleService.delete(id);
		return succ;
	}

}
