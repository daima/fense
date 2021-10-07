package com.cxy7.data.fense.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public enum ApplyStatus {

    IN_PROGRESS(1), SUCCESS(9), FAILED(-1);

    int value;
    private static final Logger logger = LoggerFactory.getLogger(ApplyStatus.class);
    ApplyStatus(int type) {
        this.value = type;
    }

    public int value() {
        return value;
    }

    public static ApplyStatus valueOf(int type) {
        ApplyStatus as = null;
        switch (type) {
            case -1:
                as = FAILED;
                break;
            case 1:
                as = IN_PROGRESS;
                break;
            case 9:
                as = SUCCESS;
                break;
            default:
                logger.warn("未找到匹配的类型：{}", type);
        }
        return as;
    }
}
