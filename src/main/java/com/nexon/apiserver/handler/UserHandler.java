package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.dao.User;
import com.nexon.apiserver.dao.NicknameValidator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017-02-04.
 */
public class UserHandler implements HttpHandler {
    private static final String SPECIAL_LETTER = "Nickname must alphanumeric but request nickname contains special letters.";
    private static final String LONGER_THAN_TWENTY = "Nickname must less than 20 characters.";
    private static final String NO_USER = "There is no user that you request.";
    private static final String ALREADY_EXIST = "Request name is already exists.";
    
    private Logger logger = Logger.getLogger(UserHandler.class);
    private SimpleMapper mapper;
    private NicknameValidator nicknameValidator;
    private JSONParser jsonParser;
    private Dao dao;
    private ResponseSender responseSender;

    public UserHandler(Dao dao, ResponseSender responseSender, SimpleMapper mapper) {
        this.mapper = mapper;
        this.dao = dao;
        this.responseSender = responseSender;
        this.jsonParser = new JSONParser();
        this.nicknameValidator = new NicknameValidator();
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        Pattern pattern = Pattern.compile("(?<=/users/).+$");
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
        String request = httpExchange.getRequestMethod();
        String response = "";
        User user;
        String[] pathVariables = pathVariable.split("/");
        switch (request) {
            case HttpMethod.GET:
                
                if (pathVariables[pathVariables.length - 1].contains("chatrooms")) {
                    List<Chatroom> chatrooms = dao.getChatRoomByUserid(Integer.parseInt(pathVariables[0]));
                    response = mapper.makeBodyFromChatrooms(chatrooms).toJSONString();
                    responseSender.sendResponse(httpExchange, response);
                }
                
                user = dao.getUser(Integer.parseInt(pathVariable, 10));
                
                if (user.getNickname() != null) {
                    response = mapper.makeBodyFromUser(user).toJSONString();
                    responseSender.sendResponse(httpExchange, response);
                } else {
                    responseSender.sendErrorResponse(httpExchange, 404, "Not Found");
                }
                
                break;
            case HttpMethod.PUT:
                user = mapper.parseBodyToUser(httpExchange.getRequestBody());
                
                if (dao.getUser(user.getNickname()).getUserid() != 0) {
                    responseSender.sendErrorResponse(httpExchange, 409, ALREADY_EXIST);
                    break;
                }
                

                switch (nicknameValidator.isValidateName(user.getNickname())) {
                    case NicknameValidator.ALPHA_NUMERIC:
                        dao.updateUser(Integer.parseInt(pathVariable, 10), user.getNickname());
                        user = dao.getUser(Integer.parseInt(pathVariable, 10));
                        response = mapper.makeBodyFromUser(user).toJSONString();
                        responseSender.sendResponse(httpExchange, response);
                        break;
                    case NicknameValidator.LONGER_THAN_TWENTY:
                        responseSender.sendErrorResponse(httpExchange, 400, LONGER_THAN_TWENTY);
                        break;
                    case NicknameValidator.SPECIAL_LETTER:
                        responseSender.sendErrorResponse(httpExchange, 400, SPECIAL_LETTER);
                        break;
                }
                break;
            case HttpMethod.DELETE:
                if (dao.getUser(Integer.parseInt(pathVariable, 10)).getUserid() != 0) {
                    dao.deleteUser(Integer.parseInt(pathVariable, 10));
                    responseSender.sendResponse(httpExchange, response);
                } else {
                    responseSender.sendErrorResponse(httpExchange, 400, NO_USER);
                }
                break;
        }
    }

    private void handleUri(HttpExchange httpExchange) {
        String request = httpExchange.getRequestMethod();

        switch (request) {
            case HttpMethod.GET:
                break;
            case HttpMethod.POST:
                User user = mapper.parseBodyToUser(httpExchange.getRequestBody());
                
                if (dao.getUser(user.getNickname()).getUserid() != 0) {
                    responseSender.sendErrorResponse(httpExchange, 409, ALREADY_EXIST);
                    break;
                }
                
                int code = nicknameValidator.isValidateName(user.getNickname());
                switch (code) {
                    case NicknameValidator.SPECIAL_LETTER:
                        responseSender.sendErrorResponse(httpExchange, 400, SPECIAL_LETTER);
                        break;
                    case NicknameValidator.LONGER_THAN_TWENTY:
                        responseSender.sendErrorResponse(httpExchange, 400, LONGER_THAN_TWENTY);
                        break;
                    case NicknameValidator.ALPHA_NUMERIC:
                        User retUser = new User();
                        int userid = dao.addUser(user.getNickname());
                        retUser.setUserid(userid);
                        retUser.setNickname(user.getNickname());
                        String response = mapper.makeBodyFromUser(retUser).toJSONString();
                        responseSender.sendResponse(httpExchange, response);
                        break;
                }
                break;
        }
    }

}