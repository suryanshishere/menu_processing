package com.shelfpulse.activation_automation.config;

import com.shelfpulse.activation_automation.util.JwtUtil;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class SocketIOConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);
    private static final int MAX_CONNECTIONS = 500;

    private final JwtUtil jwtUtil;
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final Map<String, Long> socketAdminMap = new ConcurrentHashMap<>();
    private final Map<Long, String> adminSocketMap = new ConcurrentHashMap<>();
    private final Map<String, SocketIoSocket> activeSockets = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomSockets = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> socketRooms = new ConcurrentHashMap<>();

    @Value("${app.allowed-origins:*}")
    private String allowedOrigins;

    public SocketIOConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public EngineIoServer engineIoServer() {
        EngineIoServerOptions options = EngineIoServerOptions.newFromDefault();
        options.setAllowedCorsOrigins(allowedOrigins.equals("*") ? null : allowedOrigins.split(","));
        options.setPingTimeout(60000);
        options.setPingInterval(10000); // Lower to 10s to match Node.js
        return new EngineIoServer(options);
    }

    @Bean
    public SocketIoServer socketIoServer(EngineIoServer engineIoServer) {
        return new SocketIoServer(engineIoServer);
    }

    @PostConstruct
    public void init() {
        log.info("Socket.IO server initialized on same port as HTTP server");
    }

    public void setupSocketEvents(SocketIoServer socketIoServer) {
        socketIoServer.namespace("/").on("connection", args -> {
            SocketIoSocket socket = (SocketIoSocket) args[0];

            if (connectionCount.get() >= MAX_CONNECTIONS) {
                log.warn("Max connections reached. Rejecting socket: {}", socket.getId());
                socket.send("error", "Server is currently full.");
                socket.disconnect(true);
                return;
            }

            String authHeader = getAuthHeader(socket);
            Long adminId = validateAndExtractAdminId(authHeader);

            if (adminId != null) {
                socketAdminMap.put(socket.getId(), adminId);
                adminSocketMap.put(adminId, socket.getId());
                activeSockets.put(socket.getId(), socket);
                socketRooms.put(socket.getId(), new HashSet<>());
                connectionCount.incrementAndGet();

                // Join organization room: org:{adminId}
                String orgRoom = "org:" + adminId;
                joinRoom(socket.getId(), orgRoom);
                socket.joinRoom(orgRoom);
                log.info("Socket.IO client connected: {} (adminId: {})", socket.getId(), adminId);
                // Node code doesn't explicitly send "connected" event but it's fine to keep or
                // remove. Keeping minimal.

                socket.on("join:eatery:menu:process", roomArgs -> {
                    Object arg = roomArgs[0];
                    String roomName = null;
                    try {
                        if (arg instanceof org.json.JSONObject) {
                            org.json.JSONObject json = (org.json.JSONObject) arg;
                            if (json.has("eateryId")) {
                                long eateryId = json.getLong("eateryId");
                                roomName = "org:" + adminId + ":eatery:" + eateryId;
                            }
                        } else if (arg instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> roomData = (Map<String, Object>) arg;
                            if (roomData.containsKey("eateryId")) {
                                Object eateryIdObj = roomData.get("eateryId");
                                Long eateryId = eateryIdObj instanceof Number ? ((Number) eateryIdObj).longValue()
                                        : Long.parseLong(eateryIdObj.toString());
                                roomName = "org:" + adminId + ":eatery:" + eateryId;
                            }
                        } else if (arg instanceof String) {
                            // Handle string JSON payload if necessary, similar to Node's JSON.parse
                            try {
                                org.json.JSONObject json = new org.json.JSONObject((String) arg);
                                if (json.has("eateryId")) {
                                    long eateryId = json.getLong("eateryId");
                                    roomName = "org:" + adminId + ":eatery:" + eateryId;
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        if (roomName != null) {
                            socket.joinRoom(roomName);
                            joinRoom(socket.getId(), roomName);
                            log.info("Client {} joined room: {}", socket.getId(), roomName);
                        } else {
                            log.warn("'join:eatery:menu:process' missing eateryId or invalid payload: {}", arg);
                        }
                    } catch (Exception e) {
                        log.error("Error parsing join payload", e);
                    }
                });

                socket.on("leave:eatery:menu:process", roomArgs -> {
                    Object arg = roomArgs[0];
                    String roomName = null;
                    try {
                        if (arg instanceof org.json.JSONObject) {
                            org.json.JSONObject json = (org.json.JSONObject) arg;
                            if (json.has("eateryId")) {
                                long eateryId = json.getLong("eateryId");
                                roomName = "org:" + adminId + ":eatery:" + eateryId;
                            }
                        } else if (arg instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> roomData = (Map<String, Object>) arg;
                            if (roomData.containsKey("eateryId")) {
                                Object eateryIdObj = roomData.get("eateryId");
                                Long eateryId = eateryIdObj instanceof Number ? ((Number) eateryIdObj).longValue()
                                        : Long.parseLong(eateryIdObj.toString());
                                roomName = "org:" + adminId + ":eatery:" + eateryId;
                            }
                        }

                        if (roomName != null) {
                            socket.leaveRoom(roomName);
                            leaveRoom(socket.getId(), roomName);
                            log.debug("Client {} left room: {}", socket.getId(), roomName);
                        }
                    } catch (Exception e) {
                        log.error("Error parsing leave payload", e);
                    }
                });

                socket.on("message", msgArgs -> {
                    log.debug("Received message from adminId {}: {}", adminId, msgArgs[0]);
                });

                socket.on("disconnect", disconnectArgs -> {
                    socketAdminMap.remove(socket.getId());
                    adminSocketMap.remove(adminId);
                    activeSockets.remove(socket.getId());
                    Set<String> rooms = socketRooms.remove(socket.getId());
                    if (rooms != null) {
                        for (String room : rooms) {
                            Set<String> sockets = roomSockets.get(room);
                            if (sockets != null) {
                                sockets.remove(socket.getId());
                            }
                        }
                    }
                    connectionCount.decrementAndGet();
                    log.info("Socket.IO client disconnected: {} (adminId: {})", socket.getId(), adminId);
                });
            } else {
                log.warn("Socket connection rejected - invalid auth: {}", socket.getId());
                socket.send("error", "Authentication failed");
                socket.disconnect(true);
            }
        });

    }

    private void joinRoom(String socketId, String room) {
        socketRooms.computeIfAbsent(socketId, k -> new HashSet<>()).add(room);
        roomSockets.computeIfAbsent(room, k -> ConcurrentHashMap.newKeySet()).add(socketId);
    }

    private void leaveRoom(String socketId, String room) {
        Set<String> rooms = socketRooms.get(socketId);
        if (rooms != null) {
            rooms.remove(room);
        }
        Set<String> sockets = roomSockets.get(room);
        if (sockets != null) {
            sockets.remove(socketId);
        }
    }

    private String getAuthHeader(SocketIoSocket socket) {
        Map<String, List<String>> headers = socket.getInitialHeaders();
        List<String> authList = headers.get("authorization");
        if (authList == null || authList.isEmpty()) {
            authList = headers.get("Authorization");
        }
        String authHeader = (authList != null && !authList.isEmpty()) ? authList.get(0) : null;

        if ((authHeader == null || authHeader.isEmpty()) && socket.getInitialQuery() != null) {
            String token = socket.getInitialQuery().get("token");
            if (token != null) {
                authHeader = "Bearer " + token;
            }
        }
        return authHeader;
    }

    private Long validateAndExtractAdminId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            String adminIdStr = jwtUtil.extractUsername(token);
            if (adminIdStr != null && jwtUtil.validateToken(token, adminIdStr)) {
                return Long.parseLong(adminIdStr);
            }
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return null;
    }

    public Map<String, Long> getSocketAdminMap() {
        return socketAdminMap;
    }

    public Map<Long, String> getAdminSocketMap() {
        return adminSocketMap;
    }

    public Map<String, SocketIoSocket> getActiveSockets() {
        return activeSockets;
    }

    public Map<String, Set<String>> getRoomSockets() {
        return roomSockets;
    }

    public int getConnectionCount() {
        return connectionCount.get();
    }
}
