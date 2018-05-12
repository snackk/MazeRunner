package com.mazerunner.webserver;

import com.mazerunner.webserver.handler.RegisterIpHandler;
import com.mazerunner.webserver.handler.RequestHandler;
import com.mazerunner.webserver.handler.TestHandler;
import com.mazerunner.webserver.node.MazeRunnerNodeManager;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Publisher {

    private static final int portNumber = 8000;
    private static final int threadsNumber = 20;
    private static MazeRunnerNodeManager nodeManager = null;
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(portNumber), 0);

        server.createContext("/mzrun.html", new RequestHandler());
        server.createContext("/r_ip.html", new RegisterIpHandler());
        server.createContext("/test.html",new TestHandler());

        nodeManager.registerIp(null);
        System.out.println("Publishing on: http://localhost:" + portNumber + "/mzrun.html");
        System.out.println("Publishing on: http://localhost:" + portNumber + "/r_ip.html");
        System.out.println("Publishing on: http://localhost:" + portNumber + "/test.html");
	    server.setExecutor(Executors.newFixedThreadPool(threadsNumber));
        server.start();
    }
}
