package com.arsw.bomberman.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {

    @Value("${app.socketio.port}")
    private int socketIOPort;

    @Value("${app.socketio.enabled:false}")
    private boolean socketIOEnabled;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean(destroyMethod = "stop")
    @Lazy(false)
    public SocketIOServer socketIOServer() {
        if (!socketIOEnabled) {
            return null;
        }

        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(socketIOPort);
        config.setOrigin(allowedOrigin);

        SocketIOServer server = new SocketIOServer(config);
        server.start();
        return server;
    }
}