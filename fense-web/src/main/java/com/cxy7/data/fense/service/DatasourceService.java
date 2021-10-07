package com.cxy7.data.fense.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.dao.DatasourceDao;
import com.cxy7.data.fense.model.Datasource;
import com.cxy7.data.fense.utils.AesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Service
public class DatasourceService {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private DatasourceDao datasourceDao;
    @Value("${encrypt.key.datasource.password}")
    private String KEY;

    public int add(String name, String jdbcURL, String user, String pass){
        Datasource datasource = new Datasource();
        datasource.setName(name);
        datasource.setJdbcUrl(jdbcURL);
        datasource.setUser(user);
        pass = AesUtil.encrypt(pass, KEY);
        datasource.setPass(pass);
        Date now = new Date();
        datasource.setCreateTime(now);
        datasource.setUpdateTime(now);

        return datasourceDao.save(datasource);
    }

    public JSONObject list(PageRequest pageable) {
        long total = datasourceDao.count();

        List<Datasource> datasources = datasourceDao.findAll();
        JSONArray arr = new JSONArray();
        for (Datasource datasource : datasources) {
            JSONObject obj = new JSONObject();
            obj.put("id", datasource.getId());
            obj.put("name", datasource.getName());
            obj.put("jdbcURL", datasource.getJdbcUrl());
            obj.put("user", datasource.getUser());
            obj.put("pass", datasource.getPass());
            obj.put("create_time", df.format(datasource.getCreateTime()));
            obj.put("update_time", df.format(datasource.getUpdateTime()));
            obj.put("test_url", "<a target='_blank' href='/datasource/test?id=" + datasource.getId() + "'>测试连接</a>");
            obj.put("parse_url", "<a target='_blank' href='/datasource/parse?id=" + datasource.getId() + "'>解析数据源</a>");
            arr.add(obj);
        }
        JSONObject obj = new JSONObject();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }

    public boolean edit(int id, String name, String jdbcURL, String user, String pass) {
        boolean succ = false;
        Optional<Datasource> op = datasourceDao.getOne(id);
        if (op.isPresent()) {
            Datasource datasource = op.get();
            datasource.setName(name);
            datasource.setJdbcUrl(jdbcURL);
            datasource.setUser(user);
            pass = AesUtil.encrypt(pass, KEY);
            datasource.setPass(pass);
            datasourceDao.update(datasource);
            succ = true;
        }
        return succ;
    }

    public Optional<Datasource> get(int id) {
        return datasourceDao.getOne(id);
    }

    public boolean delete(int id) {
        datasourceDao.deleteById(id);
        return true;
    }
}
