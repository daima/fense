package com.cxy7.data.fense.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.jetty.server.UserIdentity;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/7 22:09
 */
@Data
@AllArgsConstructor
public class SessionUser {
    private UserIdentity userIdentity;
    private User user;

    public static SessionUser of(UserIdentity userIdentity, User user) {
        return new SessionUser(userIdentity, user);
    }
}
