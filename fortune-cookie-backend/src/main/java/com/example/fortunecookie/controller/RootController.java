package com.example.fortunecookie.controller;

import com.example.fortunecookie.repository.FortuneRepository;
import com.example.fortunecookie.util.ApiEndpointLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @Autowired
    private FortuneRepository fortuneRepository;

    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    public RootController(FortuneRepository fortuneRepository, ApiEndpointLogger logger) {
        this.fortuneRepository = fortuneRepository;
    }

    @GetMapping("/")
    public String redirectToRandomFortune() {
        return "redirect:/fortunes/random";
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            // Simple query for checking DB connection.
            fortuneRepository.count();
            // logger.info("--> Database connection is healthy");
            return ResponseEntity.ok("Healthy");
        } catch (Exception e) {
            logger.error("--> Database connection is unhealthy", e);
            return ResponseEntity.status(500).body("Unhealthy: " + e.getMessage());
        }
    }
}
