package com.shelfpulse.activation_automation.controller;

import com.shelfpulse.activation_automation.config.WebSocketConfig.WebSocketPrincipal;
import com.shelfpulse.activation_automation.dto.websocket.JoinEateryRoomDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof WebSocketPrincipal wsPrincipal) {
            log.info("Client connected: session for adminId: {}", wsPrincipal.getAdminId());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof WebSocketPrincipal wsPrincipal) {
            log.info("Client disconnected: adminId: {}", wsPrincipal.getAdminId());
        }
    }

    @MessageMapping("/join-eatery-menu-process")
    public void joinEateryMenuProcess(@Payload JoinEateryRoomDto payload,
            SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal instanceof WebSocketPrincipal wsPrincipal) {
            Long adminId = wsPrincipal.getAdminId();
            Long eateryId = payload.getEateryId();

            if (eateryId == null) {
                log.warn(" -> 'join:eatery:menu:process' missing eateryId: {}", payload);
                return;
            }

            String roomName = "org:" + adminId + ":eatery:" + eateryId;
            log.info(" -> Joined eatery room: {}", roomName);
        }
    }

    @MessageMapping("/leave-eatery-menu-process")
    public void leaveEateryMenuProcess(@Payload JoinEateryRoomDto payload,
            SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal instanceof WebSocketPrincipal wsPrincipal) {
            Long adminId = wsPrincipal.getAdminId();
            Long eateryId = payload.getEateryId();

            if (eateryId == null) {
                log.warn(" -> 'leave:eatery:menu:process' missing eateryId: {}", payload);
                return;
            }

            String roomName = "org:" + adminId + ":eatery:" + eateryId;
            log.info(" -> Left eatery room: {}", roomName);
        }
    }

    @SubscribeMapping("/org/{adminId}/eatery/{eateryId}")
    public void subscribeToEateryRoom(SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal instanceof WebSocketPrincipal wsPrincipal) {
            log.info("   -> Subscription confirmed for adminId: {}", wsPrincipal.getAdminId());
        }
    }
}
