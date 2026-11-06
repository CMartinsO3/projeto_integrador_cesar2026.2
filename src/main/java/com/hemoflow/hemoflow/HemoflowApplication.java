package com.hemoflow.hemoflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HemoflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(HemoflowApplication.class, args);
	}

}
