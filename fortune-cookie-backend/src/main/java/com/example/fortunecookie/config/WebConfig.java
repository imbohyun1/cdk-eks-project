package com.example.fortunecookie.config;

import com.example.fortunecookie.util.ApiLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;


@Configuration
public class WebConfig {

    private final ApiLoggingInterceptor apiLoggingInterceptor;


    @Value("${cors.allowed.url}")
    private String allowedUrl;

    public WebConfig(ApiLoggingInterceptor apiLoggingInterceptor) {
        this.apiLoggingInterceptor = apiLoggingInterceptor;
    }

    @Bean
    public WebMvcConfigurer webConfigurer() {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins(allowedUrl) // Allow requests from this origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow these HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow credentials
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(apiLoggingInterceptor);
            }
        };

//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**") // Apply to all endpoints
//                        .allowedOrigins(allowedUrl) // Allow requests from this origin
//                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow these HTTP methods
//                        .allowedHeaders("*") // Allow all headers
//                        .allowCredentials(true); // Allow credentials
//            }
//        };
    }
}
