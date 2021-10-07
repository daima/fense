package com.cxy7.data.fense.server.jdbc;

import com.cxy7.data.fense.server.utils.PatchClassLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/6 17:23
 */
@Slf4j
public class ServerClassLoader extends ClassLoader {
    private PatchClassLoader loader = new PatchClassLoader();

    public void init() {
        InputStream is = ServerClassLoader.class.getResourceAsStream("/overwrite_class.properties");
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Object key : props.keySet()) {
            String name = (String) key;
            Class<?> clazz = loader.findClass(name);
            if (Objects.nonNull(clazz)) {
                log.info("loaded {}", name);
            }
            for (Class c : clazz.getDeclaredClasses()) {
                if (Objects.nonNull(c)) {
                    log.info("loaded {}", c.getCanonicalName());
                }
            }
        }
    }
}
