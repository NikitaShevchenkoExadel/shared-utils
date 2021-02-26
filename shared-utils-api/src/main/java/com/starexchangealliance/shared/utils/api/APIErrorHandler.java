package com.starexchangealliance.shared.utils.api;

import com.starexchangealliance.shared.utils.codes.KeyCodes;
import com.starexchangealliance.shared.utils.service.ServiceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Slf4j
public class APIErrorHandler implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (code != null) {
            log.error(KeyCodes.UNHANDLED_EXCEPTION + ":" + message);
            ServiceStatus serviceStatus = ServiceStatus.fromCode(Integer.valueOf(code.toString()));
            if (serviceStatus != null) {
                throw serviceStatus.withDefaultMessage();
            }
        }

        throw ServiceStatus.INTERNAL_ERROR.withDefaultMessage();
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
