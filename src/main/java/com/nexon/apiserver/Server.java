package com.nexon.apiserver;

import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.handler.ChatroomHandler;
import com.nexon.apiserver.handler.ResponseSender;
import com.nexon.apiserver.handler.UserHandler;
import com.nexon.apiserver.utils.SimpleMapper;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Server {
    private String BASE_URL;
    private String HOST;
    private int PORT;


    public static void main(String[] args) {
        Server server = new Server();
        server.initialize();
        server.start();
    }

    public void initialize() {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(new FileReader("./config.json"));
        } catch (IOException e) {
            System.out.println("Error occured when parse from config.json");
        } catch (ParseException e) {
            System.out.println("Error occured when parse from config.json. Check Json syntax");
        }
        this.BASE_URL = (String) jsonObject.get("baseurl");
        this.PORT = Integer.parseInt(String.valueOf(jsonObject.get("port")));
        this.HOST = (String) jsonObject.get("host");
    }

    public void start() {
        try {
            Dao dao = new Dao();
            ResponseSender responseSender = new ResponseSender();
            SimpleMapper mapper = new SimpleMapper();
            dao.initialize();
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/api/v1/users", new UserHandler(dao, responseSender, mapper));
            server.createContext("/api/v1/chatrooms", new ChatroomHandler(dao, responseSender, mapper));
            server.setExecutor(null); // creates a default executor
            server.start();
            
            System.out.println("Server started...");
        } catch (IOException e) {
            System.out.println("Server create failed...");
        }
    }


}
