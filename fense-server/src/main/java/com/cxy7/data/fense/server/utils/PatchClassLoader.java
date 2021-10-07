package com.cxy7.data.fense.server.utils;

import com.cxy7.data.fense.patch.Patch;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/6 18:41
 */
public class PatchClassLoader extends ClassLoader{

    @Override
    public Class<?> findClass(String name) {
        ClassLoader loader = Patch.class.getClassLoader();
        Class<?> clazz = null;
        try {
            clazz = loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }
}
