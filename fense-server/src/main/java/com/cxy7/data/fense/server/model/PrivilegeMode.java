package com.cxy7.data.fense.server.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public enum PrivilegeMode {

    SELECT("SELECT"), ALL("ALL");

    String value;
    private static final Logger logger = LoggerFactory.getLogger(PrivilegeMode.class);
    PrivilegeMode(String mode) {
        this.value = mode;
    }

    public String value() {
        return value;
    }

    public static PrivilegeMode findByMode(String mode) {
        PrivilegeMode pm = null;
        if ("SELECT".equals(mode)) {
            pm = SELECT;
        } else if ("ALL".equals(mode)) {
            pm = ALL;
        } else {
            logger.warn("未找到匹配的类型：{}", mode);
        }
        return pm;
    }
}
