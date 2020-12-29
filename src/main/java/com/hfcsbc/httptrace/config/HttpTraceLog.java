package com.hfcsbc.httptrace.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @Author: captain
 * @Date: 2020/5/6 5:22 PM
 */
public class HttpTraceLog {

    private static final Logger log = LoggerFactory.getLogger(HttpTraceLog.class);
    private static final String UTF_8_CHARSET  = "UTF-8";

    private String path;
    private String parameterMap;
    private String authorization;
    private String method;
    private Long timeTaken;
    private String time;
    private Integer status = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private String requestBody;
    private String responseBody;


    public HttpTraceLog(HttpServletRequest request, HttpServletResponse response, long startTime) {
        this.setPath(request.getRequestURI());
        this.setMethod(request.getMethod());
        this.setAuthorization(request.getHeader("Authorization"));
        long latency = System.currentTimeMillis() - startTime;
        this.setTimeTaken(latency);
        this.setTime(LocalDateTime.now().toString());
        this.setParameterMap(JSON.toJSONString(request.getParameterMap()));
        this.setStatus(response.getStatus());
        this.setRequestBody(parseRequestBody(request));
        this.setResponseBody(parseResponseBody(response));
    }

    @Override
    public String toString() {
        return "HttpTraceLog { " + '\n' +
                "  path : " + path + '\n' +
                "  parameterMap : " + parameterMap + '\n' +
                "  authorization : " + authorization + '\n' +
                "  method : " + method + '\n' +
                "  timeTaken : " + timeTaken + "ms" + '\n' +
                "  time : " + time + '\n' +
                "  status : " + status + '\n' +
                "  requestBody : " + requestBody + '\n' +
                "  responseBody : " + responseBody + '\n' +
                '}';
    }

    private String parseRequestBody(HttpServletRequest request) {
        String requestBody = "";
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            try {
                requestBody = IOUtils.toString(wrapper.getContentAsByteArray(), UTF_8_CHARSET);
            } catch (IOException e) {
                log.error("getRequestBody parse failure");
            }
        }
        return requestBody;
    }

    private String parseResponseBody(HttpServletResponse response) {
        String responseBody = "";
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            try {
                responseBody = IOUtils.toString(wrapper.getContentAsByteArray(), UTF_8_CHARSET);
            } catch (IOException e) {
                log.error("getResponseBody parse failure");
            }
        }
        return responseBody;
    }

    public String getPath() {
        return path;
    }

    public String getParameterMap() {
        return parameterMap;
    }

    public String getMethod() {
        return method;
    }

    public Long getTimeTaken() {
        return timeTaken;
    }

    public String getTime() {
        return time;
    }

    public Integer getStatus() {
        return status;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setParameterMap(String parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setTimeTaken(Long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public HttpTraceLog() {
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}
