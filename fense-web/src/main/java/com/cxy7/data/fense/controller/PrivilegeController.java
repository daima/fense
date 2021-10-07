package com.cxy7.data.fense.controller;

import com.cxy7.data.fense.model.Dataset;
import com.cxy7.data.fense.model.Privilege;
import com.cxy7.data.fense.model.PrivilegeMode;
import com.cxy7.data.fense.service.DatasetService;
import com.cxy7.data.fense.service.PrivilegeGrantService;
import com.cxy7.data.fense.service.PrivilegeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
@RequestMapping("/privilege")
public class PrivilegeController {
	private static final Logger logger = LoggerFactory.getLogger(PrivilegeController.class);
	@Autowired
	private PrivilegeService privilegeService;
	@Autowired
	private PrivilegeGrantService privilegeGrantService;
	@Autowired
	private DatasetService datasetService;

	@RequestMapping("/add")
	public boolean add(String name, String dataset_id, String mode) {
		boolean succ = false;
		if (StringUtils.isAnyBlank(name, dataset_id, mode)) {
			logger.error("参数输入错误！");
			return succ;
		}
		// TODO 检查名称是否被占用

		long datasetId = Long.parseLong(dataset_id);

		Privilege privilege = privilegeService.add(name, datasetId, mode);
		if (privilege != null) {
			logger.error("添加成功！privilegeId:" + privilege.getId());
			succ = true;
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/grant")
	public boolean grant(String oper, String id, String name, String dataset_id, String role_id, String mode) {
		boolean succ = false;
		if ("add".equals(oper)) {
			if (StringUtils.isAnyBlank(role_id)) {
				logger.error("参数输入错误！");
				return succ;
			}
			long datasetId = Long.parseLong(dataset_id);
			Privilege privilege = privilegeService.add(name, datasetId, mode);
			if (privilege == null) {
				logger.error("添加权限时出现异常");
				return succ;
			}
			int roleId = Integer.parseInt(role_id);

			privilegeGrantService.grant(privilege.getId(), roleId);

			succ = true;
		} else if ("edit".equals(oper)){
			if (StringUtils.isAnyBlank(id, name, dataset_id, role_id, mode)) {
				logger.error("ID或名称输入错误！");
				return succ;
			}

			long privilegeId = Long.parseLong(id.split("_")[0]);
			long datasetId = Long.parseLong(dataset_id);
			PrivilegeMode pm = PrivilegeMode.findByMode(mode);
			if (pm == null) {
				logger.error("不支持的权限模式：" + mode);
				return succ;
			}
			Optional<Dataset> op = datasetService.get(datasetId);
			if (!op.isPresent()) {
				logger.error("未找到该数据集：" + datasetId);
				return succ;
			}
			int roleId = Integer.parseInt(role_id);
			Optional<Privilege> privilegeOptional = privilegeService.get(privilegeId);
			if (!privilegeOptional.isPresent()) {
				logger.error("未找到该权限：" + privilegeId);
				return succ;
			}
			revoke(privilegeId, roleId);
			privilegeGrantService.grant(privilegeId, roleId);

			succ = privilegeService.update(privilegeId, name, op.get(), pm);
		} else if ("del".equals(oper)){
			if (StringUtils.isAnyBlank(id)) {
				logger.error("ID不能为空！");
				return succ;
			}
            long privilegeId = Long.parseLong(id.split("_")[0]);
			int roleId = Integer.parseInt(id.split("_")[1]);
			succ = revoke(privilegeId, roleId);
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/delete")
	public boolean revoke(long privilegeId, int roleId) {
		logger.info("正在回收权限:privilegeId: {}, roleId: {}", privilegeId, roleId);
		privilegeGrantService.revoke(privilegeId, roleId);
		return true;
	}

}
