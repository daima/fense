package com.cxy7.data.fense.server.service;

import com.cxy7.data.fense.server.dao.UserDao;
import com.cxy7.data.fense.server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Component
public class DefaultAuthenticationService {
    @Autowired
    private UserDao userDao;
    public com.cxy7.data.fense.server.model.User getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }
        User user = (User) authentication.getPrincipal();
        if (user == null) {
            return null;
        }
        return userDao.getOneByName(user.getName()).orElseThrow(() -> new UsernameNotFoundException(String.format("No user found with username '%s'.", user.getName())));
    }

}
