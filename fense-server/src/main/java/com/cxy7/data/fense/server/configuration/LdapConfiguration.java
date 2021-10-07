package com.cxy7.data.fense.server.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 11:52
 */
@Configuration
public class LdapConfiguration {
    @Value("${spring.ldap.userDN}")
    private String userDN;
    @Value("${spring.ldap.password}")
    private String password;
    @Value("${spring.ldap.base}")
    private String baseDN;
    @Value("${spring.ldap.urls}")
    private String url;
    @Value("${spring.ldap.connect.timeout}")
    private String connectTimeout;
    @Value("${spring.ldap.read.timeout}")
    private String readTimeout;

    private LdapTemplate ldapTemplate;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();

        contextSource.setUrl(url);
        contextSource.setBase(baseDN);
        contextSource.setUserDn(userDN);
        contextSource.setPassword(password);

        Map<String, Object> config = new HashMap<>();
        config.put("com.sun.jndi.ldap.connect.timeout", connectTimeout);
        config.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        config.put("java.naming.ldap.attributes.binary", "objectGUID");

        contextSource.setPooled(true);
        contextSource.setBaseEnvironmentProperties(config);
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        if (null == ldapTemplate)
            ldapTemplate = new LdapTemplate(contextSource());
        return ldapTemplate;
    }
}
