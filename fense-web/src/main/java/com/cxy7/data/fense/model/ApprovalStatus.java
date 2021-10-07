package com.cxy7.data.fense.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public enum ApprovalStatus {

    IN_PROGRESS(0), AGREE(1), DISAGREE(-1);

    int value;
    private static final Logger logger = LoggerFactory.getLogger(ApprovalStatus.class);
    ApprovalStatus(int type) {
        this.value = type;
    }

    public int value() {
        return value;
    }

    public static ApprovalStatus valueOf(int type) {
        ApprovalStatus as = null;
        switch (type) {
            case 0:
                as = IN_PROGRESS;
                break;
            case -1:
                as = DISAGREE;
                break;
            case 1:
                as = AGREE;
                break;
            default:
                logger.warn("未找到匹配的类型：{}", type);
        }
        return as;
    }
}
