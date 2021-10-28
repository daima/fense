package com.cxy7.data.fense.server.jdbc;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.server.JettyConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.calcite.avatica.AvaticaUtils;
import org.apache.calcite.avatica.metrics.MetricsSystem;
import org.apache.calcite.avatica.metrics.MetricsSystemLoader;
import org.apache.calcite.avatica.metrics.Timer;
import org.apache.calcite.avatica.metrics.noop.NoopMetricsSystem;
import org.apache.calcite.avatica.metrics.noop.NoopMetricsSystemConfiguration;
import org.apache.calcite.avatica.remote.AuthenticationType;
import org.apache.calcite.avatica.remote.Handler;
import org.apache.calcite.avatica.remote.JsonHandler;
import org.apache.calcite.avatica.remote.Service;
import org.apache.calcite.avatica.server.*;
import org.apache.calcite.avatica.util.UnsynchronizedBuffer;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.apache.calcite.avatica.remote.MetricsHelper.concat;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 13:33
 */
@Component
public class FenseJsonHandler extends AbstractAvaticaHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FenseJsonHandler.class);

    private Service service;
    private JsonHandler jsonHandler;

    private MetricsSystem metrics;
    private Timer requestTimer;

    private ThreadLocal<UnsynchronizedBuffer> threadLocalBuffer;

    private AvaticaServerConfiguration serverConfig;

    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    private ServerMeta serverMeta;
    public FenseJsonHandler(){}

    public FenseJsonHandler(Service service) {
        this(service, NoopMetricsSystem.getInstance(), null);
    }

    public FenseJsonHandler(Service service, MetricsSystem metrics) {
        this(service, metrics, null);
    }

    public FenseJsonHandler(Service service, MetricsSystem metrics,
                            AvaticaServerConfiguration serverConfig) {
        this.service = Objects.requireNonNull(service);
        this.metrics = Objects.requireNonNull(metrics);
        // Avatica doesn't have a Guava dependency
        this.jsonHandler = new JsonHandler(service, this.metrics);

        // Metrics
        this.requestTimer = this.metrics.getTimer(
                concat(FenseJsonHandler.class, MetricsAwareAvaticaHandler.REQUEST_TIMER_NAME));

        this.threadLocalBuffer = new ThreadLocal<UnsynchronizedBuffer>() {
            @Override public UnsynchronizedBuffer initialValue() {
                return new UnsynchronizedBuffer();
            }
        };

        this.serverConfig = serverConfig;
    }

    @PostConstruct
    public void init() {
        AvaticaServerConfiguration serverConfig = buildUserAuthenticationConfiguration();
        this.service = new RpcService(serverMeta);
        MetricsSystem metrics = MetricsSystemLoader.load(NoopMetricsSystemConfiguration.getInstance());
        this.metrics = Objects.requireNonNull(metrics);
        // Avatica doesn't have a Guava dependency
        this.jsonHandler = new JsonHandler(service, this.metrics);

        // Metrics
        this.requestTimer = this.metrics.getTimer(
                concat(FenseJsonHandler.class, MetricsAwareAvaticaHandler.REQUEST_TIMER_NAME));

        this.threadLocalBuffer = new ThreadLocal<UnsynchronizedBuffer>() {
            @Override public UnsynchronizedBuffer initialValue() {
                return new UnsynchronizedBuffer();
            }
        };

        this.serverConfig = serverConfig;
    }

    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String uri = request.getRequestURI();
        if (StringUtils.isNotBlank(uri) && (uri.startsWith("/api"))) {
            return;
        }
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        String jsonRequest = null;
        try (final Timer.Context ctx = requestTimer.start()) {
            if (!isUserPermitted(serverConfig, baseRequest, request, response)) {
                LOG.debug("HTTP request from {} is unauthenticated and authentication is required",
                        request.getRemoteAddr());
                return;
            }

            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            if (request.getMethod().equals("POST") && !uri.equals("/error")) {
                // First look for a request in the header, then look in the body.
                // The latter allows very large requests without hitting HTTP 413.
                String rawRequest = request.getHeader("request");
                if (rawRequest == null) {
                    // Avoid a new buffer creation for every HTTP request
                    final UnsynchronizedBuffer buffer = threadLocalBuffer.get();
                    try (ServletInputStream inputStream = request.getInputStream()) {
                        byte[] bytes = AvaticaUtils.readFullyToBytes(inputStream, buffer);
                        String encoding = request.getCharacterEncoding();
                        if (encoding == null) {
                            encoding = "UTF-8";
                        }
                        rawRequest = new String(bytes, encoding);
                    } finally {
                        // Reset the offset into the buffer after we're done
                        buffer.reset();
                    }
                }
                jsonRequest = rawRequest;
                LOG.trace("request: {}", jsonRequest);

                Handler.HandlerResponse<String> jsonResponse;
                try {
                    if (null != serverConfig && serverConfig.supportsImpersonation()) {
                        String remoteUser = serverConfig.getRemoteUserExtractor().extract(request);
                        String finalJsonRequest = jsonRequest;
                        jsonResponse = serverConfig.doAsRemoteUser(remoteUser,
                                request.getRemoteAddr(), new Callable<Handler.HandlerResponse<String>>() {
                                    @Override public Handler.HandlerResponse<String> call() {
                                        return jsonHandler.apply(finalJsonRequest);
                                    }
                                });
                    } else {
                        jsonResponse = jsonHandler.apply(jsonRequest);
                    }
                } catch (RemoteUserExtractionException e) {
                    LOG.debug("Failed to extract remote user from request", e);
                    jsonResponse = jsonHandler.unauthenticatedErrorResponse(e);
                } catch (RemoteUserDisallowedException e) {
                    LOG.debug("Remote user is not authorized", e);
                    jsonResponse = jsonHandler.unauthorizedErrorResponse(e);
                } catch (Exception e) {
                    LOG.error("Error invoking request from {}", baseRequest.getRemoteAddr(), e);
                    jsonResponse = jsonHandler.convertToErrorResponse(e);
                }

                LOG.trace("response: {}", jsonResponse);
                baseRequest.setHandled(true);
                // Set the status code and write out the response.
                response.setStatus(jsonResponse.getStatusCode());
                response.getWriter().println(jsonResponse.getResponse());
            }
        }
        if (jsonRequest != null) {
            JSONObject root = JSONObject.parseObject(jsonRequest);
            String type = Objects.requireNonNull(root.getString("request"), String.format("parse request failed. %s", jsonRequest));
            sample.stop(meterRegistry.timer("requests.timer", "type", type));
        }
    }

    public AvaticaServerConfiguration getServerConfig() {
        return serverConfig;
    }

    @Override public void setServerRpcMetadata(Service.RpcMetadataResponse metadata) {
        // Set the metadata for the normal service calls
        service.setRpcMetadata(metadata);
        // Also add it to the handler to include with exceptions
        jsonHandler.setRpcMetadata(metadata);
    }

    @Override public MetricsSystem getMetrics() {
        return metrics;
    }
    private AvaticaServerConfiguration buildUserAuthenticationConfiguration() {
        final AuthenticationType authType = AuthenticationType.BASIC;
        final String[] allowedRoles = new String[]{"users"};
        final String realm = "Avatica";

        String userPropertiesFile = null;
        try {
            userPropertiesFile = URLDecoder.decode(JettyConfig.class
                    .getResource("/auth-users.properties").getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String properties = userPropertiesFile;
        RemoteUserExtractor remoteUserExtractor = new HttpRequestRemoteUserExtractor();

        return new AvaticaServerConfiguration() {
            @Override public AuthenticationType getAuthenticationType() {
                return authType;
            }

            @Override public String[] getAllowedRoles() {
                return allowedRoles;
            }

            @Override public String getHashLoginServiceRealm() {
                return realm;
            }

            @Override public String getHashLoginServiceProperties() {
                return properties;
            }

            // Unused

            @Override public String getKerberosRealm() {
                return null;
            }

            @Override public String getKerberosPrincipal() {
                return null;
            }

            @Override public boolean supportsImpersonation() {
                return false;
            }

            @Override public <T> T doAsRemoteUser(String remoteUserName, String remoteAddress,
                                                  Callable<T> action) throws Exception {
                return null;
            }

            @Override public RemoteUserExtractor getRemoteUserExtractor() {
                return remoteUserExtractor;
            }
        };
    }
}
