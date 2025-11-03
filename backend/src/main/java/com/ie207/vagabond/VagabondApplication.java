package com.ie207.vagabond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class VagabondApplication {

	public static void main(String[] args) {
		SpringApplication.run(VagabondApplication.class, args);
	}

}
