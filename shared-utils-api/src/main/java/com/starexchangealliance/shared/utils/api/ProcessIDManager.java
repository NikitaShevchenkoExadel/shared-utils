package com.starexchangealliance.shared.utils.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

public class ProcessIDManager {

    public static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessIDManager.class);
    private static final String PROCESS_ID = "process-id";


    public static void registerNewProcessForRequest(Class<?> cls, HttpServletRequest request) {
        String parentProcessId = request.getHeader("X-Request-ID");
        String newProcessId = generateId(parentProcessId);
        setProcessId(newProcessId);
    }

    public static void registerNewProcessForRequest(Class<?> cls, HttpServletRequest request, ProcessCallback pc) {
        registerNewProcessForRequest(cls, request);

        try {
            pc.withRegisteredProcess();
        } finally {
            unregisterProcessId(cls);
        }
    }

    public static void registerNewThreadForParentProcessId(Class<?> cls, Optional<String> parentProcessId) {
        String newProcessId = generateId(parentProcessId.orElse(null));
        setProcessId(newProcessId);
    }

    public static void registerNewThreadForParentProcessId(Class<?> cls, Optional<String> parentProcessId, ProcessCallback pc) {
        registerNewThreadForParentProcessId(cls, parentProcessId);

        try {
            pc.withRegisteredProcess();
        } finally {
            unregisterProcessId(cls);
        }
    }


    public static void registerCronJobProcessId(Class<?> cls) {
        String id = "cron-job-" + cls.getSimpleName() + "-" + UUID.randomUUID();
        setProcessId(id);
    }

    public static void registerCronJobProcessId(Class<?> cls, ProcessCallback pc) {
        registerCronJobProcessId(cls);

        try {
            pc.withRegisteredProcess();
        } finally {
            unregisterProcessId(cls);
        }
    }

    public static void unregisterProcessId(Class<?> cls) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Class " + cls.getSimpleName() + " unregistered process id " + MDC.get("process-id"));
        }

        MDC.remove("process-id");
    }

    public static Optional<String> getProcessIdFromCurrentThread() {
        Optional<String> id = Optional.ofNullable(MDC.get("process-id"));
        return id;
    }

    private static void logRegistrationByClass(Class<?> cls) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Class " + cls.getSimpleName() + " started registration of new process id");
        }

    }

    private static void setProcessId(String id) {
        String old = MDC.get("process-id");
        if (old != null) {
            throw new RuntimeException("Unable to register new process id " + id + " because it has conflict with existing process id " + old + ".Please ensure that you have called unregisterProcessId method for current thread");
        } else {
            MDC.put("process-id", id);
        }
    }

    private static String generateId(String parentProcessId) {
        String childProcess = UUID.randomUUID().toString();
        return parentProcessId == null ? childProcess : parentProcessId + ">" + childProcess;
    }
    
    public interface ProcessCallback {
        void withRegisteredProcess();
    }
}
