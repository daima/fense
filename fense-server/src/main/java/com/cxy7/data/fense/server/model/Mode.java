package com.cxy7.data.fense.server.model;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/5 18:41
 */
public enum Mode {
    SELECT(1),
    UPDATE(2),
    INSERT(3),
    DELETE(4),
    ALTER(5),
    DROP(6),
    TRUNCATE(7),
    CREATE(8);

    private final int code;

    Mode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

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
