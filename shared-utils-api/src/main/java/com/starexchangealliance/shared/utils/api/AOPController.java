package com.starexchangealliance.shared.utils.api;

import com.starexchangealliance.shared.utils.codes.KeyCodes;
import com.starexchangealliance.shared.utils.service.ServiceOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class AOPController {

    private final long slowRequestLimit;
    private final long problemRequestLimit;

    private final APIResponseExceptionHandler exceptionHandler;

    public AOPController(APIResponseExceptionHandler exceptionHandler,
                         @Value("${api.warn.slowRequestLimit:10000}") long slowRequestLimit,
                         @Value("${api.warn.problemRequestLimit:30000}") long problemRequestLimit) {
        this.exceptionHandler = exceptionHandler;
        this.slowRequestLimit = slowRequestLimit;
        this.problemRequestLimit = problemRequestLimit;
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
            "|| " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public Object preProcessController(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        RequestContextProcessor rcp = new RequestContextProcessor();
        String clientIP = rcp.getClientIP();
        String httpMethod = rcp.getHttpMethod();
        String url = rcp.getFullUrl();
        String userAgent = rcp.getUserAgent();
        // TODO DDOS protection
        // TODO throttling protection
        // TODO cors

        StopWatch stopWatch = StopWatch.createStarted();
        ProcessIDManager.registerNewProcessForRequest(AOPController.class, request);
        try {
            return execute(pjp);
        } finally {
            ProcessIDManager.unregisterProcessId(AOPController.class);
            triggerOnHttpDelays(stopWatch.getTime(), httpMethod, url);
        }
    }

    private void triggerOnHttpDelays(long delay, String httpMethod, String url) {
        if (delay >= problemRequestLimit) {
            log.error(KeyCodes.PROBLEM_HTTP_REQUEST + " : " + details(delay, httpMethod, url));
        } else if (delay >= slowRequestLimit) {
            log.info(KeyCodes.SLOW_HTTP_REQUEST + " : " + details(delay, httpMethod, url));
        }
    }

    private String details(long delay, String httpMethod, String url) {
        return "[" + httpMethod + "-" + url + "] -> execution time is " + delay + " millis";
    }

    private ResponseEntity execute(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object result = pjp.proceed();
            if (result instanceof ResponseEntity) {
                return (ResponseEntity) result;
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (ServiceOutput e) {
            return exceptionHandler.handleServiceExceptions(e);
        } catch (Exception e) {
            return exceptionHandler.handleAllExceptions(e);
        } catch (Error e) {
            return exceptionHandler.handleAllErrors(e);
        }
    }

    private static class RequestContextProcessor {

        private HttpServletRequest request;

        RequestContextProcessor() {
            this.request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
        }

        private String getUrl() {
            return request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath() + request.getRequestURI();
        }

        private String getQueryString() {
            return StringUtils.trimToEmpty(request.getQueryString());
        }

        private String getFullUrl() {
            String query = getQueryString();
            return getUrl() + (StringUtils.isBlank(query) ? "" : "?" + query);
        }

        private String getClientIP() {
            return StringUtils.trimToEmpty(request.getRemoteAddr());
        }

        private String getUserAgent() {
            return StringUtils.trimToEmpty(request.getHeader(HttpHeaders.USER_AGENT));
        }

        private String getHttpMethod() {
            return StringUtils.trimToEmpty(request.getMethod());
        }
    }
}
