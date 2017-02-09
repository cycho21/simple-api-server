package com.nexon.apiserver;

import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.handler.ChatroomHandler;
import com.nexon.apiserver.handler.ResponseSender;
import com.nexon.apiserver.handler.UserHandler;
import com.nexon.apiserver.handler.SimpleMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
    private Logger logger = Logger.getLogger(Server.class);
    private String BASE_URL;
    private String HOST;
    private int PORT;

    public static void main(String[] args) {
        Server server = new Server();
        server.initialize();
        server.start();
    }

    public void initialize() {
        PropertyConfigurator.configure("log4j.properties");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
    
        logger.info("Chatting API server initialized...");
        
        try {
            jsonObject = (JSONObject) jsonParser.parse(new FileReader("./config.json"));
        } catch (IOException e) {
            logger.error("Error occured when parse from config.json. File not found.");
        } catch (ParseException e) {
            logger.error("Error occured when parse from config.json. Check Json syntax.");
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
            server.createContext("/api/v1/fortest", httpExchange -> {
                logger.info("Delete all tables, and recreate for test");
                dao.initialize();     
            });
            server.setExecutor(null); // creates a default executor
            server.start();
            
            logger.info("Chatting API server started...");
            
        } catch (IOException e) {
            logger.error("HttpServer.create() makes this error. Check host.");
        }
    }


}
