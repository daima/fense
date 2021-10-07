package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.model.Approval;
import com.cxy7.data.fense.model.Role;
import com.cxy7.data.fense.model.User;
import com.cxy7.data.fense.security.DefaultAuthenticationService;
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
@RequestMapping("/approval")
public class 	ApprovalController {
	private static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);
	@Autowired
	private ApprovalService approvalService;
	@Autowired
	private DefaultAuthenticationService authenticationService;
	@Autowired
	private RoleService roleService;

	@ResponseBody
	@RequestMapping(value = "list")
	public JSONObject list(int page, int rows) {
		if (page < 0 || rows < 0) {
			return new JSONObject();
		}
		PageRequest pageable = PageRequest.of(page - 1, rows);

		User approver = authenticationService.getCurrentUser();
		return approvalService.list(pageable, approver.getId());
	}

	@ResponseBody
	@RequestMapping(value = "/edit")
	public boolean edit(String oper, String id, String role, String new_role, String opinion) {
		boolean succ = false;
		if (!"edit".equals(oper) && !"del".equals(oper)) {
			logger.warn("unknown oper:{}", oper);
			return succ;
		}
		if (!NumberUtils.isDigits(id)) {
			logger.error("参数输入错误！{}", id);
			return succ;
		}
		long approval_id = Long.parseLong(id);
		Optional<Approval> op = approvalService.get(approval_id);
		if (!op.isPresent()) {
			logger.error("审批单不存在！{}", approval_id);
			return succ;
		}
		Approval approval = op.get();
		if ("del".equals(oper)) {
			return approvalService.disagree(approval);
		}
		if (StringUtils.isBlank(opinion)) {
			logger.warn("审批意见为空");
			opinion = "同意";
		}
		Role tmpRole = null;
		if (StringUtils.isNotBlank(new_role)) {
			// 新建角色
			tmpRole = roleService.add(new_role);
		} else if (NumberUtils.isDigits(role)) {
			int roleId = Integer.parseInt(role);
			Optional<Role> roleOptional = roleService.get(roleId);
			if (!roleOptional.isPresent()) {
				logger.error("角色不存在");
				return succ;
			}
			tmpRole = roleOptional.get();
		} else {
			logger.error("参数错误：role:{}, new_role:{}", role, new_role);
			return succ;
		}
		// 创建权限
		// 将权限赋给角色
		return approvalService.agree(approval, tmpRole.getId(), opinion);
	}

}
