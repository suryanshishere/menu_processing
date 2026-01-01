package com.shelfpulse.activation_automation.service;

import com.shelfpulse.activation_automation.dto.websocket.MenuProcessingStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyMenuProcessingStatus(Long adminId, Long eateryId, MenuProcessingStatusDto data) {
        String destination = buildEateryDestination(adminId, eateryId);
        log.info("📢 Emitting 'menu:processing:status' to {}", destination);
        messagingTemplate.convertAndSend(destination + "/menu-processing-status", data);
    }

    public void notifyEateryRoom(Long adminId, Long eateryId, String eventType, Object data) {
        String destination = buildEateryDestination(adminId, eateryId);
        log.info("📢 Emitting '{}' to {}", eventType, destination);
        messagingTemplate.convertAndSend(destination + "/" + eventType, data);
    }

    public void notifyOrganization(Long adminId, String eventType, Object data) {
        String destination = "/topic/org/" + adminId;
        log.info("📢 Emitting '{}' to {}", eventType, destination);
        messagingTemplate.convertAndSend(destination + "/" + eventType, data);
    }

    public void notifyUser(Long adminId, String eventType, Object data) {
        log.info("📢 Sending user message '{}' to adminId: {}", eventType, adminId);
        messagingTemplate.convertAndSendToUser(String.valueOf(adminId), "/queue/" + eventType, data);
    }

    public void sendError(Long adminId, String message) {
        log.warn("⚠️ Sending error to adminId {}: {}", adminId, message);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(adminId),
                "/queue/errors",
                new ErrorMessage(message));
    }

    private String buildEateryDestination(Long adminId, Long eateryId) {
        return "/topic/org/" + adminId + "/eatery/" + eateryId;
    }

    public record ErrorMessage(String message) {
    }
}
