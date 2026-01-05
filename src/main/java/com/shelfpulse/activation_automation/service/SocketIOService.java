package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.config.SocketIOConfig;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SocketIOService {

    private static final Logger log = LoggerFactory.getLogger(SocketIOService.class);

    private final SocketIoServer socketIoServer;
    private final SocketIOConfig socketIOConfig;

    public SocketIOService(SocketIoServer socketIoServer, SocketIOConfig socketIOConfig) {
        this.socketIoServer = socketIoServer;
        this.socketIOConfig = socketIOConfig;
    }

    @PostConstruct
    public void init() {
        socketIOConfig.setupSocketEvents(socketIoServer);
        log.info("Socket.IO service initialized");
    }

    public void sendToAdmin(Long adminId, String event, Object... data) {
        String socketId = socketIOConfig.getAdminSocketMap().get(adminId);
        if (socketId != null) {
            SocketIoSocket socket = socketIOConfig.getActiveSockets().get(socketId);
            if (socket != null) {
                socket.send(event, data);
                log.debug("Sent event '{}' to adminId: {}", event, adminId);
            }
        }
    }

    public void sendToRoom(String room, String event, Object... data) {
        Set<String> socketIds = socketIOConfig.getRoomSockets().get(room);
        if (socketIds != null) {
            for (String socketId : socketIds) {
                SocketIoSocket socket = socketIOConfig.getActiveSockets().get(socketId);
                if (socket != null) {
                    socket.send(event, data);
                }
            }
        }
        log.debug("Sent event '{}' to room: {}", event, room);
    }

    public void broadcast(String event, Object... data) {
        for (SocketIoSocket socket : socketIOConfig.getActiveSockets().values()) {
            socket.send(event, data);
        }
        log.debug("Broadcast event '{}'", event);
    }

    public int getActiveConnections() {
        return socketIOConfig.getConnectionCount();
    }

    public SocketIoServer getServer() {
        return socketIoServer;
    }
}
