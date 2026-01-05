package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.websocket.MenuProcessingStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);
    private final SocketIORedisAdapter socketIORedisAdapter;

    public WebSocketNotificationService(SocketIORedisAdapter socketIORedisAdapter) {
        this.socketIORedisAdapter = socketIORedisAdapter;
    }

    public void notifyMenuProcessingStatus(Long adminId, Long eateryId, MenuProcessingStatusDto data) {
        String room = buildEateryRoom(adminId, eateryId);
        log.info("📢 Emitting 'menu:processing:status' to room {}", room);
        socketIORedisAdapter.publishToRoom(room, "menu:processing:status", data);
    }

    public void notifyEateryRoom(Long adminId, Long eateryId, String eventType, Object data) {
        String room = buildEateryRoom(adminId, eateryId);
        log.info("📢 Emitting '{}' to room {}", eventType, room);
        socketIORedisAdapter.publishToRoom(room, eventType, data);
    }

    public void notifyOrganization(Long adminId, String eventType, Object data) {
        String room = "org_" + adminId;
        log.info("📢 Emitting '{}' to room {}", eventType, room);
        socketIORedisAdapter.publishToRoom(room, eventType, data);
    }

    public void notifyUser(Long adminId, String eventType, Object data) {
        log.info("📢 Sending '{}' to adminId: {}", eventType, adminId);
        socketIORedisAdapter.publishToAdmin(adminId, eventType, data);
    }

    public void sendError(Long adminId, String message) {
        log.warn("⚠️ Sending error to adminId {}: {}", adminId, message);
        socketIORedisAdapter.publishToAdmin(adminId, "error", Map.of("message", message));
    }

    private String buildEateryRoom(Long adminId, Long eateryId) {
        return "eatery_" + adminId + "_" + eateryId;
    }
}
