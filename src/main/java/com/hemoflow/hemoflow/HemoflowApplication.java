package com.hemoflow.hemoflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.hemoflow\\.hemoflow\\.api\\.controller\\..*"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.hemoflow\\.hemoflow\\.service\\..*")
})
@EnableJpaRepositories(basePackages = "com.hemoflow.hemoflow.persistencia")
@EntityScan(basePackages = "com.hemoflow.hemoflow.dominio")
public class HemoflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(HemoflowApplication.class, args);
	}

}
