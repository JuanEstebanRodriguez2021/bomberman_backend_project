package com.arsw.bomberman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;

@SpringBootApplication
public class BombermanApplication {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(BombermanApplication.class);
		String socket = System.getenv("SOCKETIO_ENABLED");
		if ("true".equalsIgnoreCase(socket)){
			app.setWebApplicationType(WebApplicationType.REACTIVE);
		}
		app.run(args);
	}

}
