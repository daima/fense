package com.cxy7.data.fense.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.service.DatasetService;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Controller
@RequestMapping("/dataset")
public class DatasetController {
	private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);
	@Autowired
	private DatasetService datasetService;

	@ResponseBody
	@RequestMapping(value = "/get")
	public JSONObject get(String id) {
		if (!NumberUtils.isDigits(id)) {
			logger.error("参数错误！{}", id);
			return null;
		}
		long datasetId = Long.parseLong(id);
		JSONObject obj = datasetService.getDataset(datasetId);
		return obj;
	}

	@ResponseBody
	@RequestMapping(value = "/list")
	public JSONArray list() {
//		String content = "";
//		try {
//			content = FileUtils.readFileToString(new File("E:\\Src\\fense\\fense-web\\src\\main\\resources\\dataset.json"), "utf-8");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return JSONArray.parseArray(content);
		return datasetService.list();
	}

	@ResponseBody
	@RequestMapping(value = "/updateOwner")
	public boolean updateOwner(String id, String owner) {
		if (!NumberUtils.isDigits(id) || !NumberUtils.isDigits(owner)) {
			logger.error("参数错误！id:{}, owner:{}", id, owner);
			return false;
		}
		long dataset_id = Long.parseLong(id);
		int ownerId = Integer.parseInt(owner);
		return datasetService.updateOwner(dataset_id, ownerId);
	}

}
