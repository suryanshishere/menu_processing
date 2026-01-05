package com.shelfpulse.activation_automation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final int MAX_CONNECTIONS = 25;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    @Value("${app.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins);
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns(origins)
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024);
        registration.setSendBufferSizeLimit(512 * 1024);
        registration.setSendTimeLimit(20000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    if (activeConnections.get() >= MAX_CONNECTIONS) {
                        throw new IllegalStateException(
                                "Server is currently full. Max connections: " + MAX_CONNECTIONS);
                    }

                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String token = authHeaders.get(0).replace("Bearer ", "");
                        Long adminId = validateTokenAndGetAdminId(token);
                        if (adminId != null) {
                            accessor.setUser(new WebSocketPrincipal(adminId));
                            activeConnections.incrementAndGet();
                        } else {
                            throw new IllegalArgumentException("Invalid authentication token");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing Authorization header");
                    }
                } else if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    activeConnections.decrementAndGet();
                }

                return message;
            }
        });
    }

    private Long validateTokenAndGetAdminId(String token) {
        // TODO: Integrate with your JWT/security service to validate token and extract
        // adminId
        // This should use your existing security configuration
        // Example: return jwtService.validateAndGetAdminId(token);
        return 1L; // Placeholder - replace with actual validation
    }

    public static class WebSocketPrincipal implements Principal {
        private final Long adminId;

        public WebSocketPrincipal(Long adminId) {
            this.adminId = adminId;
        }

        @Override
        public String getName() {
            return String.valueOf(adminId);
        }

        public Long getAdminId() {
            return adminId;
        }
    }
}
