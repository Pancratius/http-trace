package com.hfcsbc.httptrace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * @Author: captain
 * @Date: 2020/4/27 5:04 PM
 */
@Configuration
public class HttpTraceLogFilter extends OncePerRequestFilter implements Ordered {

    @Value("${httpTraceLogFilter.ignoreUrls}")
    private String ignoreUrls = "/export,/heath";

    @Value("${httpTraceLogFilter.ignoreMethods}")
    private String ignoreRequestMethods = "GET";

    private static final Logger log = LoggerFactory.getLogger(HttpTraceLogFilter.class);

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /** 忽略匹配规则的请求 */
        if (ifIgnoreIntercept(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info(new HttpTraceLog(request, response, startTime).toString());
            updateResponse(response);
        }

    }

    private boolean ifIgnoreIntercept(HttpServletRequest request) {

        try {
            /** 是否可以解析URI */
            new URI(request.getRequestURL().toString());

            /** 路径 */
            boolean ifIgnoreUrl = matchIgnoreCondition(this.ignoreUrls, request.getRequestURI());

            /** 请求方式 */
            boolean ifIgnoreMethod = matchIgnoreCondition(this.ignoreRequestMethods, request.getMethod());

            return ifIgnoreUrl || ifIgnoreMethod;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean matchIgnoreCondition(String ignoreConditions, String condition) {
        if (ignoreConditions.isEmpty() || condition.isEmpty()) {
            return false;
        }
        return Stream.of(ignoreConditions.split(",")).anyMatch(ignoreCondition -> (condition.indexOf(ignoreCondition) > -1));
    }


    private void updateResponse(HttpServletResponse response) throws IOException {

        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            Objects.requireNonNull(responseWrapper).copyBodyToResponse();
        }
    }
}

