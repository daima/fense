package com.cxy7.data.fense.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.dao.DatasetDao;
import com.cxy7.data.fense.dao.DatasourceDao;
import com.cxy7.data.fense.dao.UserDao;
import com.cxy7.data.fense.model.Dataset;
import com.cxy7.data.fense.model.DatasetType;
import com.cxy7.data.fense.model.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DatasetService {
    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private DatasourceDao datasourceDao;
    @Autowired
    private UserDao userDao;

    public Dataset addDb(Datasource datasource, long parent, String name){
        return add(datasource, parent, name, DatasetType.DB);
    }

    public Dataset addTable(Datasource datasource, long parent, String name){
        return add(datasource, parent, name, DatasetType.TABLE);
    }
    public boolean updateOwner(long id, int owner) {
        Dataset dataset = datasetDao.getOne(id).get();
        dataset.setOwner(owner);
        datasetDao.save(dataset);
        return true;
    }

    public Dataset add(Datasource datasource, long parent, String name, DatasetType type){
        Dataset dataset = new Dataset();
        dataset.setParent(parent);
        dataset.setName(name);
        dataset.setType(type.value());
        dataset.setDatasourceId(datasource.getId());
        Date now = new Date();
        dataset.setCreateTime(now);
        dataset.setUpdateTime(now);
        datasetDao.save(dataset);
        return dataset;
    }

    public JSONArray list() {
        JSONArray dsArr = new JSONArray();
        List<Datasource> datasources = datasourceDao.findAll();
        for (Datasource ds : datasources) {
            int dsId = ds.getId();
            JSONObject dsObj = new JSONObject();
            dsObj.put("id", dsId);
            dsObj.put("text", ds.getName());
            dsObj.put("icon", "fa fa-cubes");
            JSONArray dbArr = new JSONArray();
            List<Dataset> dbs = datasetDao.findAllByTypeAndDatasource(DatasetType.DB.value(), dsId);
            for (Dataset db : dbs) {
                long dbId = db.getId();
                JSONObject dbObj = new JSONObject();
                dbObj.put("id", dbId);
                dbObj.put("text", db.getName());
                dbObj.put("icon", "fa fa-database");
                List<Dataset> tbls = datasetDao.findAllByParent(dbId);
                JSONArray tblArr = new JSONArray();
                for (Dataset tbl : tbls) {
                    JSONObject tblObj = new JSONObject();
                    tblObj.put("id", tbl.getId());
                    tblObj.put("text", tbl.getName());
                    tblObj.put("icon", "fa fa-table");
                    tblArr.add(tblObj);
                }
                dbObj.put("children", tblArr);
                dbArr.add(dbObj);
            }
            dsObj.put("children", dbArr);
            dsArr.add(dsObj);
        }
        return dsArr;
    }

    public JSONObject getDataset(long id) {
        Dataset dataset = datasetDao.getOne(id).orElseThrow(() -> new IllegalArgumentException("dataset does not exist" + id));
        int datasourceId = dataset.getDatasourceId();
        Datasource datasource = datasourceDao.getOne(datasourceId).orElseThrow(() -> new IllegalArgumentException("dataset does not exist" + datasourceId));
        JSONObject obj = new JSONObject();
        obj.put("id", dataset.getId());
        obj.put("name", dataset.getName());
        obj.put("type", DatasetType.valueOf(dataset.getType()).name());
        obj.put("update_time", df.format(dataset.getUpdateTime()));
        obj.put("datasource_name", datasource.getName());
        obj.put("owner", dataset.getOwner());
        return obj;
    }

    public boolean delete(long id) {
        datasetDao.deleteById(id);
        return true;
    }

    public Optional<Dataset> get(long id) {
        return datasetDao.getOne(id);
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }
}
