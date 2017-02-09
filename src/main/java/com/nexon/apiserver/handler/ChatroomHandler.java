package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.dao.Message;
import com.nexon.apiserver.dao.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chan8 on 2017-02-07.
 */
public class ChatroomHandler implements HttpHandler {
    private static final String ALREADY_EXIST = "Request name is already exists.";
    private static final String NOT_YOURS = "Request chatroom was not make by you.";
    private static final String NOT_JOINED = "You are not joined that room.";

    private Logger logger = Logger.getLogger(ChatroomHandler.class);
    private ResponseSender responseSender;
    private JSONParser jsonParser;
    private Dao dao;
    private SimpleMapper mapper;

    public ChatroomHandler(Dao dao, ResponseSender responseSender, SimpleMapper mapper) {
        this.mapper = mapper;
        this.dao = dao;
        this.responseSender = responseSender;
        this.jsonParser = new JSONParser();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("(?<=/chatrooms/).+$");
        Matcher matcher = pattern.matcher(path);
        boolean hasPathVariable = matcher.find();

        logger.info(":: UserHandler handle request :: URI : " + httpExchange.getRequestURI());
        logger.info(":: Requestmethod : " + httpExchange.getRequestMethod() + " ::");
        
        if (hasPathVariable == true)
            handlePathVariableUri(httpExchange, matcher.group());
        else
            handleUri(httpExchange);
    }

    private void handlePathVariableUri(HttpExchange httpExchange, String pathVariable) {
        String requestMethod = httpExchange.getRequestMethod();
        String response = "";
        String[] pathVariables = pathVariable.split("/");
        
        if (pathVariable.endsWith("messages/") || pathVariable.endsWith("messages")) {
            handleMessages(httpExchange, pathVariable);
            return;
        }
            
        switch (requestMethod) {
            
            case HttpMethod.POST:
                /*
                    /chatrooms/{chatroomid}/users case
                 */
                if (pathVariables[pathVariables.length - 1].contains("users")) {
                    int userid = mapper.parseBodyToUserWithID(httpExchange.getRequestBody()).getUserid();
                    dao.joinChatroom(userid, Integer.parseInt(pathVariables[0], 10));
                    responseSender.sendResponse(httpExchange, "");
                }
                break;
                
            case HttpMethod.PUT:
                Chatroom chatroom = mapper.parseBodyToChatroom(httpExchange.getRequestBody());

                if (dao.getChatRoomByNameById(Integer.parseInt(pathVariables[0])).getChatroomname().equals(chatroom.getChatroomname())) {
                    responseSender.sendErrorResponse(httpExchange, 409, ALREADY_EXIST);
                    break;
                }

                if (chatroom.getUserid() == dao.getChatRoomByNameById(Integer.parseInt(pathVariables[0])).getUserid()) {
                    int chatroomid = dao.updateChatroom(chatroom.getChatroomname(), chatroom.getUserid());
                    chatroom.setChatroomid(chatroomid);
                } else {
                    responseSender.sendErrorResponse(httpExchange, 403, NOT_YOURS);
                    break;
                }
                
                response = mapper.makeBodyFromChatroom(chatroom).toJSONString();
                responseSender.sendResponse(httpExchange, response);
                break;
                
            case HttpMethod.DELETE:
                int chatroomid = Integer.parseInt(pathVariables[0], 10);
                /*
                    TO DO : delete last /
                 */
                int userid = Integer.parseInt(pathVariables[2], 10);
                List<Chatroom> chatrooms = dao.getChatRoomByUserid(userid);
                
                boolean isJoined = false;
                for (Chatroom c : chatrooms) {
                    if (chatroomid == c.getChatroomid())
                        isJoined = true;
                }

                if (isJoined == false) {
                    responseSender.sendErrorResponse(httpExchange, 404, NOT_JOINED);
                    break;
                } else {
                    dao.quitChatroom(chatroomid, userid);
                    responseSender.sendResponse(httpExchange, "");
                    break;
                }
            case HttpMethod.GET:
                chatroomid = Integer.parseInt(pathVariables[0], 10);
                ArrayList<User> users = dao.getChatroomJoiner(chatroomid);
                response = mapper.makeBodyFromUsers(users).toJSONString();
                responseSender.sendResponse(httpExchange, response);
                break;
            }
        }

    private void handleMessages(HttpExchange httpExchange, String pathVariable) {
        String requestMethod = httpExchange.getRequestMethod();
        String response = "";
        String[] pathVariables = pathVariable.split("/");
        
        switch (requestMethod) {
            case HttpMethod.POST:
                Message message = mapper.parseBodyToMessage(httpExchange.getRequestBody());
                int messageid = dao.postMessage(message.getSenderid(), message.getReceiverid(), Integer.parseInt(pathVariables[0], 10), message.getMessageBody());
                message = dao.checkMessage(messageid);
                if (message.getMessageid() != 0) {
                    response = mapper.makeBodyFromMessage(message).toJSONString();
                    responseSender.sendResponse(httpExchange, response);
                }
                break;
            case HttpMethod.GET:
                int userid = Integer.parseInt(httpExchange.getRequestHeaders().get("Userid").get(0));
                ArrayList<Message> messageArrayList = dao.getMessagesByUserId(Integer.parseInt(pathVariables[0], 10), userid);
                response = mapper.makeBodyFromMessages(messageArrayList).toJSONString();
                responseSender.sendResponse(httpExchange, response);
                break;
        }
    }

    private void handleUri(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();
        switch (requestMethod) {
            case HttpMethod.POST:
                Chatroom chatroom = mapper.parseBodyToChatroom(httpExchange.getRequestBody());
                
                if (chatroom.getChatroomname().length() > 100) {
                    chatroom.setChatroomname(chatroom.getChatroomname().substring(0, 100));
                }

                if (dao.getChatRoomByNameById(chatroom.getChatroomname()).getChatroomid() != 0) {
                    responseSender.sendErrorResponse(httpExchange, 409, ALREADY_EXIST);
                    break;
                }

                int chatroomid = dao.addChatRoom(chatroom.getChatroomname(), chatroom.getUserid());
                chatroom.setChatroomid(chatroomid);
                dao.joinChatroom(chatroom.getUserid(), chatroom.getChatroomid());
                String response = mapper.makeBodyFromChatroom(chatroom).toJSONString();
                responseSender.sendResponse(httpExchange, response);
                break;
            case HttpMethod.GET:
                User user = mapper.parseBodyToUser(httpExchange.getRequestBody());
                List<Chatroom> chatrooms = dao.getChatRoomByUserid(user.getUserid());
                response = mapper.makeBodyFromChatrooms(chatrooms).toJSONString();
                responseSender.sendResponse(httpExchange, response);
                break;
            default:
                break;
        }
    }
    
}
