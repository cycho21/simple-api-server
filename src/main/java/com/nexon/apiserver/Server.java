package com.nexon.apiserver;

import com.nexon.apiserver.handler.RootHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Server {

    public static void main(String[] args) {
        Server server = new Server();
        server.start(8000);
    }

    private void start(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/test", new RootHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            System.out.println("Server create failed...");
        }
    }


}
