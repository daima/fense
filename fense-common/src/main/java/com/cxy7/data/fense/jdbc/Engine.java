package com.cxy7.data.fense.jdbc;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 16:47
 */
public enum Engine {
    MYSQL,
    HIVE,
    CLICKHOUSE,
    TIDB,
    DORIS;

    public static <T extends Enum<?>> T lookup(String search) {
        String clazz = new Object() {
            public String getClassName() {
                String clazzName = this.getClass().getName();
                return clazzName.substring(0, clazzName.lastIndexOf('$'));
            }
        }.getClassName();
        Class<T> enumeration = null;
        try {
            enumeration = (Class<T>) Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }
}
