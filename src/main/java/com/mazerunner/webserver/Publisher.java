package com.mazerunner.webserver;

import com.mazerunner.webserver.handler.RegisterIpHandler;
import com.mazerunner.webserver.handler.RequestHandler;
import com.mazerunner.webserver.handler.TestHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Publisher {

    private static final int portNumber = 8000;
    private static final int threadsNumber = 20;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(portNumber), 0);

        server.createContext("/mzrun.html", new RequestHandler());
        server.createContext("/r_ip.html", new RegisterIpHandler());
	server.createContext("/test.html",new TestHandler());

        System.out.println("Publishing on: http://localhost:" + portNumber + "/mzrun.html");
        System.out.println("Publishing on: http://localhost:" + portNumber + "/r_ip.html");
        System.out.println("Publishing on: http://localhost:" + portNumber + "/test.html");
	    server.setExecutor(Executors.newFixedThreadPool(threadsNumber));
        server.start();
    }
}
