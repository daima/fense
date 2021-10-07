package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.model.Apply;
import com.cxy7.data.fense.model.User;
import com.cxy7.data.fense.security.DefaultAuthenticationService;
import com.cxy7.data.fense.service.ApplyService;
import com.cxy7.data.fense.service.ApprovalService;
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
@RequestMapping("/apply")
public class ApplyController {
	private static final Logger logger = LoggerFactory.getLogger(ApplyController.class);
	@Autowired
	private ApplyService applyService;
	@Autowired
	private ApprovalService approvalService;
	@Autowired
	private DefaultAuthenticationService authenticationService;

	@RequestMapping("/add")
	public boolean add(String applicant, String privilege_id, String reason) {
		boolean succ = false;
		if (StringUtils.isAnyBlank(applicant, privilege_id, reason)) {
			logger.error("参数输入错误！");
			return succ;
		}
		// TODO 检查名称是否被占用

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

		return applyService.list(pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/edit")
	public boolean edit(String oper, String id, String dataset_id, String mode, String name, String reason) {
		User applicant = authenticationService.getCurrentUser();
		boolean succ = false;
		if ("add".equals(oper)) {
			if (StringUtils.isAnyBlank(name, dataset_id, mode)) {
				logger.error("参数输入错误！");
				return succ;
			}
			long datasetId = Long.parseLong(dataset_id);
			Apply apply = applyService.add(applicant, datasetId, name, mode);
			if (apply != null) {
				// 创建审批单
				// TODO 下一个审批者，需要一个审批链条来支持
				approvalService.add(apply, datasetId);
			}
			succ = true;
		} else if ("edit".equals(oper)){
			if (StringUtils.isAnyBlank(id, reason)) {
				logger.error("参数输入错误！id:{}, reason:{}", id, reason);
				return succ;
			}
			if (!NumberUtils.isDigits(id)) {
				logger.error("参数输入错误！id:{}", id);
				return succ;
			}
			long applyId = Long.parseLong(id);
			applyService.update(applyId, reason);
		} else if ("del".equals(oper)){
			if (StringUtils.isBlank(id)) {
				logger.error("ID不能为空！");
				return succ;
			}
			int applyId = Integer.parseInt(id);
			succ = delete(applyId);
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/delete")
	public boolean delete(int id) {
		logger.info("正在删除角色:{}", id);
		boolean succ = applyService.delete(id);
		return succ;
	}

}
