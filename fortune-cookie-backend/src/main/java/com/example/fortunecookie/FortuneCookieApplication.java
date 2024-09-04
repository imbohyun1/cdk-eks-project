package com.example.fortunecookie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.xray.spring.aop.XRayEnabled;

@SpringBootApplication
//@XRayEnabled
public class FortuneCookieApplication {

	public static void main(String[] args) {
		SpringApplication.run(FortuneCookieApplication.class, args);
	}

}
