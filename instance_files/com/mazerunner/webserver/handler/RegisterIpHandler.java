package com.mazerunner.webserver.handler;

import java.io.IOException;
import java.io.OutputStream;

import com.mazerunner.webserver.node.MazeRunnerNodeManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegisterIpHandler implements HttpHandler{

    private MazeRunnerNodeManager mazeRunnerNodeManager;

    @Override
    public void handle(HttpExchange t) throws IOException {
        mazeRunnerNodeManager = mazeRunnerNodeManager.getInstance();

        String machineIp = t.getRequestURI().getQuery().split("=")[1];
        System.out.println("Request to register IP: " + machineIp);

        mazeRunnerNodeManager.registerIp(machineIp);

        t.sendResponseHeaders(200, machineIp.length());
        OutputStream os = t.getResponseBody();
        os.write(machineIp.getBytes());
        os.close();
    }
}
