package com.cxy7.data.fense.controller;

import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.model.Datasource;
import com.cxy7.data.fense.service.DatasourceService;
import com.cxy7.data.fense.utils.DatasourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
@RequestMapping("/datasource")
public class DatasourceController {
	private static final Logger logger = LoggerFactory.getLogger(DatasourceController.class);
	@Autowired
	private DatasourceService datasourceService;
	@Autowired
	private DatasourceUtils datasourceUtils;

	@RequestMapping("/add")
	public boolean add(String name, String jdbcURL, String user, String pass) {
		boolean succ = false;
		if (StringUtils.isAnyBlank(name)) {
			logger.error("名称输入错误！");
			return succ;
		}
		// TODO 检查名称是否被占用

		int id = datasourceService.add(name, jdbcURL, user, pass);
		logger.error("添加成功！datasourceId:" + id);
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

		return datasourceService.list(pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/edit")
	public boolean edit(String oper, String id, String name, String jdbcURL, String user, String pass) {
		boolean succ = false;
		if ("add".equals(oper)) {
			if (StringUtils.isAnyBlank(name, jdbcURL, user, pass)) {
				logger.error("名称输入错误！");
				return succ;
			}
			succ = add(name, jdbcURL, user, pass);
		} else if ("edit".equals(oper)){
			if (StringUtils.isAnyBlank(id, name, jdbcURL, user, pass)) {
				logger.error("ID或名称输入错误！");
				return succ;
			}
			int datasourceId = Integer.parseInt(id);
			succ = datasourceService.edit(datasourceId, name, jdbcURL, user, pass);
		} else if ("del".equals(oper)){
			if (StringUtils.isBlank(id)) {
				logger.error("ID不能为空！");
				return succ;
			}
			int datasourceId = Integer.parseInt(id);
			succ = delete(datasourceId);
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/delete")
	public boolean delete(int id) {
		logger.info("正在删除datasource:{}", id);
		boolean succ = datasourceService.delete(id);
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/test")
	public boolean test(String id) {
		boolean succ = false;
		if (!NumberUtils.isDigits(id)) {
			logger.error("ID错误！");
			return succ;
		}
		int datasourceId = Integer.parseInt(id);
		Optional<Datasource> op = datasourceService.get(datasourceId);
		if (!op.isPresent()) {
			logger.error("ID错误！");
			return succ;
		}
		Datasource ds = op.get();

		Connection connection = datasourceUtils.getConnection(ds);
		try {
			if (connection != null) {
				List<Object> param = new ArrayList<>();
				List<Map<String, Object>> list = JdbcUtils.executeQuery(connection, "SHOW DATABASES;", param);
				if (list != null && !list.isEmpty()) {
					logger.info("测试成功!");
					succ = true;
				}
				for (Map<String, Object> map : list) {
					logger.info("db:{}", map.get("Database"));
				}
				connection.close();
			}
		} catch (SQLException e) {
			logger.error("", e);
		}
		return succ;
	}

	@ResponseBody
	@RequestMapping(value = "/parse")
	public boolean parse(String id) {
		boolean succ = false;
		if (!NumberUtils.isDigits(id)) {
			logger.error("ID错误！");
			return succ;
		}
		int datasourceId = Integer.parseInt(id);
		Optional<Datasource> op = datasourceService.get(datasourceId);
		if (!op.isPresent()) {
			logger.error("ID错误！");
			return succ;
		}
		Datasource ds = op.get();
		succ = datasourceUtils.parseDatasource(ds);
		return succ;
	}


}
