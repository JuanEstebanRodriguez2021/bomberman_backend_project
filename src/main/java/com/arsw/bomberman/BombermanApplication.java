package com.arsw.bomberman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BombermanApplication {

	public static void main(String[] args) {
		String socketEnabled = System.getenv("SOCKETIO_ENABLED");
		if ("true".equalsIgnoreCase(socketEnabled)) {
			String port = System.getenv("PORT");
			int tomcatPort = 9999;
			System.setProperty("server.port", String.valueOf(tomcatPort));
		}
		SpringApplication.run(BombermanApplication.class, args);
	}

}
