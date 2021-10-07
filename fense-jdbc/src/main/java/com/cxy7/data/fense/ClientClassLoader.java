package com.cxy7.data.fense;

import com.cxy7.data.fense.patch.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/6 17:23
 */
public class ClientClassLoader extends ClassLoader {
    private static Logger log = LoggerFactory.getLogger(ClientClassLoader.class);
    public void init() {
        URL url = Patch.class.getResource("");
        String classpath = url.getPath();
        classpath = classpath.substring(0, classpath.indexOf("classes/") + "classes/".length());

        String libPath = url.getPath();
        int idx = libPath.indexOf(".jar");
        if (idx != -1) {
            libPath = libPath.substring(0, idx + ".jar".length());
        }

        PatchClassLoader loader = null;
        try {
            loader = new PatchClassLoader(libPath, classpath);
        } catch (NoSuchMethodException | MalformedURLException e) {
            e.printStackTrace();
        }
        InputStream is = ClientClassLoader.class.getResourceAsStream("/overwrite_class.properties");
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
