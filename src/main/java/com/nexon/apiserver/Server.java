package com.nexon.apiserver;

import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.handler.ChatroomHandler;
import com.nexon.apiserver.handler.RootHandler;
import com.nexon.apiserver.handler.UserHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Server {
    private static final String BASE_URL = "/api/v1";
    

    public static void main(String[] args) {
        Server server = new Server();
        server.start(8000);
    }

    public void start(int port) {
        try {
            Dao dao = new Dao();
//            dao.dropUsersTable();
//            dao.createUsersTable();
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/testuri", new RootHandler());
            server.createContext("/api/v1/users", new UserHandler(dao));
            server.createContext("/api/v1/chatrooms", new ChatroomHandler(dao));
            server.setExecutor(null); // creates a default executor
            server.start();
            
            System.out.println("Server started...");
        } catch (IOException e) {
            System.out.println("Server create failed...");
        }
    }


}
