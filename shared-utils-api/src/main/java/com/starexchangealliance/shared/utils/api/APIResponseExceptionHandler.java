package com.starexchangealliance.shared.utils.api;

import com.starexchangealliance.shared.utils.api.dto.ErrorResponseBody;
import com.starexchangealliance.shared.utils.api.dto.error.Message;
import com.starexchangealliance.shared.utils.api.dto.error.PropertyMessage;
import com.starexchangealliance.shared.utils.codes.KeyCodes;
import com.starexchangealliance.shared.utils.service.PropertyServiceMessage;
import com.starexchangealliance.shared.utils.service.ServiceMessage;
import com.starexchangealliance.shared.utils.service.ServiceOutput;
import com.starexchangealliance.shared.utils.service.ServiceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Slf4j
public class APIResponseExceptionHandler implements Ordered {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @ExceptionHandler({Error.class})
    public ResponseEntity<Object> handleAllErrors(Error e) {
        log.error(KeyCodes.JVM_ERROR, e);
        String message = formatMessage(e);
        return status(ServiceStatus.INTERNAL_ERROR.getHttpCode()).body(response(message));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllExceptions(Exception e) {
        log.error(KeyCodes.UNHANDLED_EXCEPTION, e);
        String message = formatMessage(e);
        return status(ServiceStatus.INTERNAL_ERROR.getHttpCode()).body(response(message));
    }

    private String formatMessage(Throwable t) {
        return "dev".equalsIgnoreCase(activeProfile) || "test".equalsIgnoreCase(activeProfile) ?
                t.getMessage() + " <---- Visible only for DEV|TEST profile. For fast debug not for tests" :
                ServiceStatus.INTERNAL_ERROR.getDefaultMessage();
    }

    @ExceptionHandler({ServiceOutput.class})
    public ResponseEntity<Object> handleServiceExceptions(ServiceOutput e) {
        Object object = e.getObject();

        if (e.getApiCode().getHttpCode() >= 500) {
            log.error(KeyCodes.INTERNAL_ERROR, e);
        }

        ResponseEntity.BodyBuilder result = status(e.getApiCode().getHttpCode());
        if (object instanceof List<?>) {
            List<Message> objects = ((List<?>)object).stream().map(this::repack).collect(Collectors.toList());
            return result.body(response(objects));
        } else if (object instanceof ServiceMessage || object instanceof Message) {
            Message objects = repack(object);
            return result.body(response(objects));
        } else {
            return result.body(response(String.valueOf(object)));
        }
    }

    private Message repack(Object original) {
        if (original instanceof PropertyServiceMessage) {
            PropertyServiceMessage o = (PropertyServiceMessage) original;
            return PropertyMessage.of(o.getProperty(), o.getMessage());
        } else if (original instanceof ServiceMessage) {
            ServiceMessage o = (ServiceMessage) original;
            return Message.of(o.getMessage());
        } else {
            throw new RuntimeException("Unknown how to handle object " +
                    (original == null ? "NULL" : original.getClass()));
        }
    }

    private ErrorResponseBody response(String message) {
        return response(singletonList(Message.of(message)));
    }

    private ErrorResponseBody response(Message propertyIssue) {
        return response(singletonList(propertyIssue));
    }

    private ErrorResponseBody response(List<Message> e) {
        return new ErrorResponseBody(e);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
