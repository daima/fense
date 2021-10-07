package com.cxy7.data.fense.jdbc.utils;

/**
 * @Author: XiaoYu Cai
 * @Date: 2021/4/12 8:05 下午
 */
public class Version {
    public static final String PRODUCT_NAME = "Fense";
    public static final String NAME = "fense-jdbc";
    public static final boolean JDBC_COMPLIANT = true;
    /** project version */
    public static final String VERSION = "${project.version}";
    /** SCM(git) revision */
    public static final String SCM_REVISION= "${buildNumber}";
    /** SCM branch */
    public static final String SCM_BRANCH = "${scmBranch}";
    /** build timestamp */
    public static final String TIMESTAMP ="${buildtimestamp}";

    public static int majorVersion() {
        return Integer.parseInt(VERSION.substring(0, VERSION.indexOf('.')));
    }

    public static int minorVersion() {
        int idx = VERSION.indexOf('-') != -1 ? VERSION.indexOf('-') : VERSION.length();
        return Integer.parseInt(VERSION.substring(VERSION.indexOf('.') + 1, idx));
    }
}
