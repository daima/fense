package com.cxy7.data.fense.server;

import com.cxy7.data.fense.server.jdbc.ServerClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/12 23:49
 */
@SpringBootApplication
@ComponentScan({"com.cxy7.data.fense.server", "com.cxy7.data.fense.mail"})
public class FenseServer {
    private static Logger LOG = LoggerFactory.getLogger(FenseServer.class);

    public static void main(String[] args) {
        ServerClassLoader loader = new ServerClassLoader();
        loader.init();
        SpringApplication.run(FenseServer.class, args);
    }
}
