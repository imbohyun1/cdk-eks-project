package com.example.fortunecookie.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

@Component
public class ApiEndpointLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApiEndpointLogger.class);

    private final RequestMappingHandlerMapping handlerMapping;

    public ApiEndpointLogger(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        logger.info("=== Registered API Endpoints ===");
        handlerMethods.forEach((key, value) -> {
            Set<String> patterns = key.getPathPatternsCondition() != null ?
                    key.getPathPatternsCondition().getPatternValues() :
                    key.getPatternsCondition().getPatterns();

            patterns.forEach(pattern -> {
                logger.info("{} {} : {}",
                        value.getMethod().getDeclaringClass().getSimpleName(),
                        value.getMethod().getName(),
                        pattern);
            });
        });
        logger.info("================================");
    }
}