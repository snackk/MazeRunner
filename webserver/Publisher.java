package webserver;

import webserver.mss.MSSManager;
import webserver.handler.RegisterIpHandler;
import webserver.handler.RequestHandler;
import webserver.handler.TestHandler;
import webserver.node.HealthCheck;
import webserver.node.MazeRunnerNodeManager;
import webserver.node.AutoScalerCheck;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.Executors;

public class Publisher {

    private static final int portNumber = 8000;
    private static final int threadsNumber = 20;
    private static MazeRunnerNodeManager nodeManager = MazeRunnerNodeManager.getInstance();
	
    private static MSSManager mssmanager = MSSManager.getInstance();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(portNumber), 0);
	
        server.createContext("/mzrun.html", new RequestHandler());
        server.createContext("/r_ip.html", new RegisterIpHandler());
        server.createContext("/test.html",new TestHandler());
        
        // Register IPs from Nodes
        nodeManager.registerIps();
        
        System.out.println("Publishing on: http://localhost:" + portNumber + "/mzrun.html");
        System.out.println("Publishing on: http://localhost:" + portNumber + "/r_ip.html");
        System.out.println("Publishing on: http://localhost:" + portNumber + "/test.html");
	    server.setExecutor(Executors.newFixedThreadPool(threadsNumber));
        server.start();

        /*Perform health checks each minute*/
        Timer t1 = new Timer();
        Timer t2 = new Timer();
        t1.schedule(new HealthCheck(), 0,60000);
        t2.schedule(new AutoScalerCheck(), 0,60000*5);

    }
}
