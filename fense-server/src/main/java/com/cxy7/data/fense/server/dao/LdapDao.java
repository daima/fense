package com.cxy7.data.fense.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 11:58
 */
@Component
public class LdapDao {
    @Autowired
    private LdapTemplate ldapTemplate;

    public boolean login(String username, String password) {
        EqualsFilter filter = new EqualsFilter("cn", username);
        return ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
    }
}
