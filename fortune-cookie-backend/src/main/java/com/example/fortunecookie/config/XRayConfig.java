package com.example.fortunecookie.config;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.spring.aop.AbstractXRayInterceptor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@ConditionalOnProperty(name = "aws.xray.enabled", havingValue = "true", matchIfMissing = false)
public class XRayConfig extends AbstractXRayInterceptor {

    @Override
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void xrayEnabledClasses() {}

    @Bean
    public AWSXRayServletFilter xrayServletFilter() {
        return new AWSXRayServletFilter("MyApp");
    }
}
