package com.arsw.bomberman.sockets;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.arsw.bomberman.auth.JwtService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class SocketGateway {

    private static final Logger logger = LoggerFactory.getLogger(SocketGateway.class);

    private final SocketIOServer server;
    private final JwtService jwtService;

    public SocketGateway(@Nullable SocketIOServer server, JwtService jwtService) {
        this.server = server;
        this.jwtService = jwtService;
    }

    @PostConstruct
    public void registerHandlers() {
        if (server == null) {
            logger.info("Socket.IO deshabilitado en este Web Service (SOCKETIO_ENABLED=false)");
            return;
        }

        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);
    }

    private void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");

        if (token == null || !jwtService.isValid(token)) {
            logger.warn("Conexión de socket rechazada: token inválido o ausente");
            client.disconnect();
            return;
        }

        Long userId = jwtService.extractUserId(token);
        client.set("userId", userId);
        logger.info("Cliente conectado por socket userId={} sessionId={}", userId, client.getSessionId());
    }

    private void onDisconnect(SocketIOClient client) {
        Object userId = client.get("userId");
        logger.info("Cliente desconectado userId={} sessionId={}", userId, client.getSessionId());
    }
}