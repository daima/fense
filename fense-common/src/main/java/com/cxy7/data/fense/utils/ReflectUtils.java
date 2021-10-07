package com.cxy7.data.fense.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 17:22
 */
@Slf4j
public class ReflectUtils {
    @Nullable
    public static Object getFieldValue(Object object, String fieldName) {
        Field field;
        Object value = null;
        try {
            field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            value = field.get(object);
        } catch (NoSuchFieldException e) {
            log.warn("get field failed: {}", fieldName, e);
        } catch (IllegalAccessException e) {
            log.warn("get field failed: {}", fieldName, e);
        }
        return value;
    }
}
