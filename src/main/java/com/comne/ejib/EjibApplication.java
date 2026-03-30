package com.comne.ejib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EjibApplication {

	public static void main(String[] args) {
		SpringApplication.run(EjibApplication.class, args);
	}

}
