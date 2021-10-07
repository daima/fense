package com.cxy7.data.fense.server;

import com.cxy7.data.fense.server.jdbc.FenseJsonHandler;
import com.cxy7.data.fense.server.service.LoginService;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 11:34 上午
 */
@Configuration
public class JettyConfig {
    private static Logger LOG = LoggerFactory.getLogger(JettyConfig.class);
    @Autowired
    private LoginService loginService;
    @Value("${server.rpc.serialization:JSON}")
    private String serialization;

    @Autowired
    private FenseJsonHandler fenseJsonHandler;

    @Bean
    public JettyServletWebServerFactory jettyServletWebServerFactory(
            JettyServerCustomizer jettyServerCustomizer) {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        factory.addServerCustomizers(jettyServerCustomizer);
        return factory;
    }

    @Bean
    public JettyServerCustomizer jettyCustomizer() {
        return (server) -> {
            try {
                Class.forName("com.cxy7.data.fense.server.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                LOG.error("", e);
            }
            Handler avaticaHandler = getAvaticaHandler(server);

            WebAppContext context = (WebAppContext)server.getHandler();
            Handler[] handlers = context.getHandlers();
            Handler[] allHandlers = new Handler[handlers.length + 1];
            allHandlers[0] = avaticaHandler;
            System.arraycopy(handlers, 0, allHandlers, 1, handlers.length);
            HandlerList handlerList = new HandlerList();
            handlerList.setHandlers(allHandlers);

            context.setHandler(handlerList);
        };
    }

    private Handler getAvaticaHandler(Server server) {
        Handler avaticaHandler = fenseJsonHandler;

        // Wrap the provided handler for security if we made one
        if (null != fenseJsonHandler.getServerConfig()) {
            final String[] allowedRoles = new String[]{"users"};
            server.addBean(loginService);

            ConstraintSecurityHandler securityHandler = configureCommonAuthentication(Constraint.__BASIC_AUTH,
                    allowedRoles, new BasicAuthenticator(), null, loginService);
            securityHandler.setHandler(avaticaHandler);
            avaticaHandler = securityHandler;
        }

        return avaticaHandler;
    }

    protected ConstraintSecurityHandler configureCommonAuthentication(String constraintName,
                                                                      String[] allowedRoles, Authenticator authenticator, String realm,
                                                                      LoginService loginService) {
        Constraint healthConstraint = new Constraint();
        healthConstraint.setName(constraintName);
        healthConstraint.setRoles(allowedRoles);
        healthConstraint.setAuthenticate(false);

        ConstraintMapping healthCm = new ConstraintMapping();
        healthCm.setConstraint(healthConstraint);
        healthCm.setPathSpec("/actuator/*");

        Constraint constraint = new Constraint();
        constraint.setName(constraintName);
        constraint.setRoles(allowedRoles);
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
        sh.setAuthenticator(authenticator);
        sh.setLoginService(loginService);
        sh.setConstraintMappings(new ConstraintMapping[]{healthCm, cm});
        sh.setRealmName(realm);

        return sh;
    }
}
