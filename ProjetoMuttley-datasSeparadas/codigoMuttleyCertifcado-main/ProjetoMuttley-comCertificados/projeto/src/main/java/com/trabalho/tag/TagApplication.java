package com.trabalho.tag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TagApplication {

	public static void main(String[] args) {
		SpringApplication.run(TagApplication.class, args);
	}

}
