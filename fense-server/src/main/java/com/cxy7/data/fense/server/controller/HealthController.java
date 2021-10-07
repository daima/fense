package com.cxy7.data.fense.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 1:09 下午
 */
@RestController
public class HealthController {
    @RequestMapping(value = "/actuator/healthCheck")
    public int health() {
        return 1;
    }
}
