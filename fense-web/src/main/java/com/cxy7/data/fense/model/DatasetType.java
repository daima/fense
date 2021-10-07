package com.cxy7.data.fense.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public enum DatasetType {

    NYX(-1), DB(1), TABLE(2);

    int value;
    private static final Logger logger = LoggerFactory.getLogger(DatasetType.class);
    DatasetType(int type) {
        this.value = type;
    }

    public int value() {
        return value;
    }

    public static DatasetType valueOf(int type) {
        DatasetType dt = null;
        switch (type) {
            case -1:
                dt = NYX;
                break;
            case 1:
                dt = DB;
                break;
            case 2:
                dt = TABLE;
                break;
            default:
                logger.warn("未找到匹配的类型：{}", type);
        }
        return dt;
    }
}
