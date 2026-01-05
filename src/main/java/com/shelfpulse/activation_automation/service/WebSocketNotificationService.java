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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public WebSocketNotificationService(SocketIORedisAdapter socketIORedisAdapter,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.socketIORedisAdapter = socketIORedisAdapter;
        this.objectMapper = objectMapper;
    }

    public void notifyMenuProcessingStatus(Long adminId, Long eateryId, MenuProcessingStatusDto data) {
        String room = buildEateryRoom(adminId, eateryId);
        log.info("📢 Emitting 'menu:processing:status' to room {}", room);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(data, Map.class);
            org.json.JSONObject jsonObject = new org.json.JSONObject(map);
            socketIORedisAdapter.publishToRoom(room, "menu:processing:status", jsonObject);
        } catch (Exception e) {
            log.error("Failed to convert message data to JSONObject", e);
        }
    }

    public void notifyEateryRoom(Long adminId, Long eateryId, String eventType, Object data) {
        String room = buildEateryRoom(adminId, eateryId);
        log.info("📢 Emitting '{}' to room {}", eventType, room);
        try {
            Object payload = data;
            if (data instanceof Map) {
                payload = new org.json.JSONObject((Map<?, ?>) data);
            } else if (!(data instanceof String) && !(data instanceof Number) && !(data instanceof Boolean)
                    && !(data instanceof org.json.JSONObject)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.convertValue(data, Map.class);
                payload = new org.json.JSONObject(map);
            }
            socketIORedisAdapter.publishToRoom(room, eventType, payload);
        } catch (Exception e) {
            log.error("Failed to convert message data to JSONObject", e);
        }
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
        return "org:" + adminId + ":eatery:" + eateryId;
    }
}
