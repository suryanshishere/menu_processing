package com.shelfpulse.activation_automation.config;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.utils.ParseQS;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

@Configuration
public class EngineIoWebSocketConfig {

    private final EngineIoServer engineIoServer;

    public EngineIoWebSocketConfig(EngineIoServer engineIoServer) {
        this.engineIoServer = engineIoServer;
    }

    @Bean
    public FilterRegistrationBean<Filter> engineIoFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new EngineIoServletFilter(engineIoServer));
        registration.addUrlPatterns("/socket.io/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public ServerEndpointConfig engineIoWebSocketEndpoint() {
        return ServerEndpointConfig.Builder
                .create(EngineIoWebSocketEndpoint.class, "/socket.io/")
                .configurator(new ServerEndpointConfig.Configurator() {
                    @Override
                    public <T> T getEndpointInstance(Class<T> clazz) {
                        return clazz.cast(new EngineIoWebSocketEndpoint(engineIoServer));
                    }

                    @Override
                    public void modifyHandshake(ServerEndpointConfig sec,
                            jakarta.websocket.server.HandshakeRequest request,
                            jakarta.websocket.HandshakeResponse response) {
                        sec.getUserProperties().put("query", request.getQueryString());
                        sec.getUserProperties().put("headers", request.getHeaders());
                    }
                })
                .build();
    }

    public static class EngineIoServletFilter implements Filter {
        private final EngineIoServer engineIoServer;

        public EngineIoServletFilter(EngineIoServer engineIoServer) {
            this.engineIoServer = engineIoServer;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String transport = httpRequest.getParameter("transport");
            if (transport != null && transport.equals("websocket")) {
                chain.doFilter(request, response);
            } else {
                engineIoServer.handleRequest(httpRequest, httpResponse);
            }
        }
    }

    public static class EngineIoWebSocketEndpoint extends Endpoint {
        private final EngineIoServer engineIoServer;
        private EngineIoWebSocket webSocket;

        public EngineIoWebSocketEndpoint(EngineIoServer engineIoServer) {
            this.engineIoServer = engineIoServer;
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            Map<String, Object> userProps = config.getUserProperties();
            String queryString = (String) userProps.get("query");
            @SuppressWarnings("unchecked")
            Map<String, List<String>> headers = (Map<String, List<String>>) userProps.get("headers");

            Map<String, String> query = ParseQS.decode(queryString);

            webSocket = new EngineIoWebSocket() {
                @Override
                public Map<String, String> getQuery() {
                    return query;
                }

                @Override
                public Map<String, List<String>> getConnectionHeaders() {
                    return headers;
                }

                @Override
                public void write(String message) throws IOException {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(message);
                    }
                }

                @Override
                public void write(byte[] message) throws IOException {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
                    }
                }

                @Override
                public void close() {
                    try {
                        session.close();
                    } catch (IOException e) {
                    }
                }
            };

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    webSocket.emit("message", message);
                }
            });

            session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
                @Override
                public void onMessage(ByteBuffer message) {
                    byte[] bytes = new byte[message.remaining()];
                    message.get(bytes);
                    webSocket.emit("message", bytes);
                }
            });

            engineIoServer.handleWebSocket(webSocket);
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            if (webSocket != null) {
                webSocket.emit("close");
            }
        }

        @Override
        public void onError(Session session, Throwable throwable) {
            if (webSocket != null) {
                webSocket.emit("error", "WebSocket error", throwable.getMessage());
            }
        }
    }
}
