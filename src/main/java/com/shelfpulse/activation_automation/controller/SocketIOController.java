package com.shelfpulse.activation_automation.controller;

import io.socket.engineio.server.EngineIoServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SocketIOController {

    private final EngineIoServer engineIoServer;

    public SocketIOController(EngineIoServer engineIoServer) {
        this.engineIoServer = engineIoServer;
    }

    @RequestMapping("/socket.io/")
    public void handleSocketIO(HttpServletRequest request, HttpServletResponse response) throws IOException {
        engineIoServer.handleRequest(request, response);
    }
}
