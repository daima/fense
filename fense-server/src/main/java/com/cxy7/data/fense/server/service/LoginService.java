package com.cxy7.data.fense.server.service;

import com.cxy7.data.fense.server.model.SessionUser;
import com.cxy7.data.fense.server.model.User;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 11:51 上午
 */
@Service
public class LoginService extends AbstractLoginService {
    private static Logger LOG = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private UserService userService;
    private volatile Cache<String, SessionUser> sessionUserCache;
    public LoginService () {
        sessionUserCache = CacheBuilder.newBuilder()
                .concurrencyLevel(10)
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .removalListener((RemovalListener<String, SessionUser>) notification -> {
                    LOG.info("Removal Listener " + notification.getKey());
                })
                .build();
    }
    @Override
    public UserIdentity login(String username, Object credentials, ServletRequest request) {
        Preconditions.checkNotNull(username, "username required.");
        SessionUser su = sessionUserCache.getIfPresent(username);
        if (Objects.nonNull(su)) {
            return su.getUserIdentity();
        }

        String plainPassword = (String) credentials;
        User user = userService.findByNameAndPassword(username, plainPassword);
        if (!Objects.nonNull(user)) {
            LOG.warn("login failed: {}", username);
            return null;
        }
        UserPrincipal userPrincipal = new UserPrincipal(username, Credential.getCredential(plainPassword));
        Subject subject = new Subject();
        subject.getPrincipals().add(userPrincipal);

        String[] roles = loadRoleInfo(userPrincipal);
        if (roles != null) {
            for (String role : roles) {
                subject.getPrincipals().add(new RolePrincipal(role));
            }
        }
        subject.setReadOnly();
        UserIdentity userIdentity = _identityService.newUserIdentity(subject, userPrincipal, roles);
        sessionUserCache.put(username, SessionUser.of(userIdentity, user));

        return userIdentity;
    }

    @Override
    protected String[] loadRoleInfo(UserPrincipal userPrincipal) {
        return new String[]{"users"};
    }

    @Override
    protected UserPrincipal loadUserInfo(String username) {
        return null;
    }
    public User getUser(String username) {
        SessionUser sessionUser = sessionUserCache.getIfPresent(username);
        return sessionUser.getUser();
    }
}
