package com.shelfpulse.activation_automation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class SocketIORedisAdapter implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(SocketIORedisAdapter.class);
    private static final String CHANNEL_PREFIX = "socketio:";
    private static final String BROADCAST_CHANNEL = CHANNEL_PREFIX + "broadcast";
    private static final String ROOM_CHANNEL_PREFIX = CHANNEL_PREFIX + "room:";
    private static final String ADMIN_CHANNEL_PREFIX = CHANNEL_PREFIX + "admin:";

    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final SocketIOService socketIOService;
    private final ObjectMapper objectMapper;

    public SocketIORedisAdapter(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            SocketIOService socketIOService,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.socketIOService = socketIOService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this, new ChannelTopic(BROADCAST_CHANNEL));
        listenerContainer.addMessageListener(this, new ChannelTopic(ROOM_CHANNEL_PREFIX + "*"));
        listenerContainer.addMessageListener(this, new ChannelTopic(ADMIN_CHANNEL_PREFIX + "*"));
        log.info("Socket.IO Redis adapter initialized with instanceId: {}", instanceId);
    }

    @PreDestroy
    public void destroy() {
        listenerContainer.removeMessageListener(this);
        log.info("Socket.IO Redis adapter destroyed");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            SocketIOMessage msg = objectMapper.readValue(message.getBody(), SocketIOMessage.class);

            if (instanceId.equals(msg.sourceInstanceId)) {
                return;
            }

            log.debug("Received message from Redis channel: {} from instance: {}", channel, msg.sourceInstanceId);

            if (channel.equals(BROADCAST_CHANNEL)) {
                socketIOService.broadcast(msg.event, msg.data);
            } else if (channel.startsWith(ROOM_CHANNEL_PREFIX)) {
                String room = channel.substring(ROOM_CHANNEL_PREFIX.length());
                socketIOService.sendToRoom(room, msg.event, msg.data);
            } else if (channel.startsWith(ADMIN_CHANNEL_PREFIX)) {
                Long adminId = Long.parseLong(channel.substring(ADMIN_CHANNEL_PREFIX.length()));
                socketIOService.sendToAdmin(adminId, msg.event, msg.data);
            }
        } catch (Exception e) {
            log.error("Failed to process Redis message: {}", e.getMessage());
        }
    }

    public void publishToAdmin(Long adminId, String event, Object... data) {
        socketIOService.sendToAdmin(adminId, event, data);
        publish(ADMIN_CHANNEL_PREFIX + adminId, event, data);
    }

    public void publishToRoom(String room, String event, Object... data) {
        socketIOService.sendToRoom(room, event, data);
        publish(ROOM_CHANNEL_PREFIX + room, event, data);
    }

    public void publishBroadcast(String event, Object... data) {
        socketIOService.broadcast(event, data);
        publish(BROADCAST_CHANNEL, event, data);
    }

    private void publish(String channel, String event, Object[] data) {
        try {
            SocketIOMessage msg = new SocketIOMessage(instanceId, event, data);
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(msg));
            log.debug("Published to Redis channel: {}", channel);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for Redis: {}", e.getMessage());
        }
    }

    public void subscribeToRoom(String room) {
        listenerContainer.addMessageListener(this, new ChannelTopic(ROOM_CHANNEL_PREFIX + room));
        log.debug("Subscribed to room channel: {}", room);
    }

    public void unsubscribeFromRoom(String room) {
        listenerContainer.removeMessageListener(this, new ChannelTopic(ROOM_CHANNEL_PREFIX + room));
        log.debug("Unsubscribed from room channel: {}", room);
    }

    private static class SocketIOMessage {
        public String sourceInstanceId;
        public String event;
        public Object[] data;

        public SocketIOMessage() {
        }

        public SocketIOMessage(String sourceInstanceId, String event, Object[] data) {
            this.sourceInstanceId = sourceInstanceId;
            this.event = event;
            this.data = data;
        }
    }
}
