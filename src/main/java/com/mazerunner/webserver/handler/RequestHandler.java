package com.mazerunner.webserver.handler;

import com.mazerunner.webserver.node.MazeRunnerNodeManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class RequestHandler implements HttpHandler {

    private MazeRunnerNodeManager mazeRunnerNodeManager;

    @Override
    public void handle(HttpExchange t) throws IOException {
        mazeRunnerNodeManager = MazeRunnerNodeManager.getInstance();
        String query = t.getRequestURI().getQuery();

        System.out.println("Request to solve maze with parameters: " + query);
        String response = mazeRunnerNodeManager.solveMazeOnNode(query);

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
