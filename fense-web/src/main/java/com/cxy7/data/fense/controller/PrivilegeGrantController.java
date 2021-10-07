package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.model.User;
import com.cxy7.data.fense.security.DefaultAuthenticationService;
import com.cxy7.data.fense.service.DatasetService;
import com.cxy7.data.fense.service.PrivilegeGrantService;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/privilege_grant")
public class PrivilegeGrantController {
	private static final Logger logger = LoggerFactory.getLogger(PrivilegeGrantController.class);
	@Autowired
	private PrivilegeGrantService privilegeGrantService;
	@Autowired
	private DefaultAuthenticationService authenticationService;
	@Autowired
	private DatasetService datasetService;

	/**
	 * 根据datasetId查询对该Dataset有权限的角色列表
	 */
	@ResponseBody
	@RequestMapping(value = "/listByDatasetId")
	public JSONObject listByDatasetId(int page, int rows, String id) {
		if (StringUtils.isBlank(id)) {
			logger.warn("dataset_id不能为空");
			return new JSONObject();
		}
		if (page < 0 || rows < 0) {
			return new JSONObject();
		}
		PageRequest pageable = PageRequest.of(page - 1, rows);
		long datasetId = Long.parseLong(id);
		return privilegeGrantService.listByDatasetId(pageable, datasetId);
	}

	@ResponseBody
	@RequestMapping(value = "/listByDatasetIdAndUser")
	public JSONObject listByDatasetIdAndUser(int page, int rows , String id) {
		if (StringUtils.isBlank(id)) {
			logger.warn("dataset_id不能为空");
			return new JSONObject();
		}
		if (page < 0 || rows < 0) {
			return new JSONObject();
		}
		PageRequest pageable = PageRequest.of(page - 1, rows);
		long datasetId = Long.parseLong(id);

		User user = authenticationService.getCurrentUser();
		return privilegeGrantService.listByDatasetIdAndUser(pageable, datasetId, user.getId());
	}

}
