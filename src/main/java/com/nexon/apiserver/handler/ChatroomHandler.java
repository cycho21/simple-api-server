package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.dao.User;
import com.nexon.apiserver.utils.MappingUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    private JSONParser jsonParser;
    private Dao dao;

    public ChatroomHandler(Dao dao) {
        this.dao = dao;
        this.jsonParser = new JSONParser();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("(?<=/chatrooms/).+$");
        Matcher matcher = pattern.matcher(path);
        boolean hasPathVariable = matcher.find();
        
        if (hasPathVariable == true)
            handlePathVariableUri(httpExchange, matcher.group());
        else
            handleUri(httpExchange);
    }

    private void handlePathVariableUri(HttpExchange httpExchange, String pathVariable) {
        String requestMethod = httpExchange.getRequestMethod();
        String response = "";
        String[] pathVariables = pathVariable.split("/");
        
        switch (requestMethod) {
            
            case HttpMethod.POST:
                /*
                    /chatrooms/{chatroomid}/users case
                 */
                if (pathVariables[pathVariables.length - 1].contains("users")) {
                    int userid = parseBodyToUser(httpExchange.getRequestBody()).getUserid();
                    dao.joinChatroom(userid, Integer.parseInt(pathVariables[0], 10));
                    sendResponse(httpExchange, "");
                }
                break;
                
            case HttpMethod.PUT:
                Chatroom chatroom = parseBodyToChatroom(httpExchange.getRequestBody());

                if (dao.getChatRoom(Integer.parseInt(pathVariables[0])).getChatroomname().equals(chatroom.getChatroomname())) {
                    sendErrorResponse(httpExchange, 409, ALREADY_EXIST);
                    break;
                }

                if (chatroom.getUserid() == dao.getChatRoom(Integer.parseInt(pathVariables[0])).getUserid()) {
                    chatroom = dao.updateChatroom(chatroom.getChatroomname(), chatroom.getUserid());
                } else {
                    sendErrorResponse(httpExchange, 403, NOT_YOURS);
                    break;
                }
                
                response = MappingUtils.makeBodyFromChatroom(chatroom).toJSONString();
                System.out.println(response);
                sendResponse(httpExchange, response);
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
                    sendErrorResponse(httpExchange, 404, NOT_JOINED);
                    break;
                } else {
                    dao.quitChatroom(chatroomid, userid);
                    sendResponse(httpExchange, "");
                    break;
                }
            case HttpMethod.GET:
                chatroomid = Integer.parseInt(pathVariables[0], 10);
                ArrayList<User> users = dao.getChatroomJoiner(chatroomid);
                response = MappingUtils.makeBodyFromUsers(users).toJSONString();
                sendResponse(httpExchange, response);
                break;
            }
        }

    private void handleUri(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();
        switch (requestMethod) {
            case HttpMethod.POST:
                Chatroom chatroom = parseBodyToChatroom(httpExchange.getRequestBody());
                
                if (chatroom.getChatroomname().length() > 100) {
                    chatroom.setChatroomname(chatroom.getChatroomname().substring(0, 100));
                }

                if (dao.getChatRoom(chatroom.getChatroomname()).getChatroomid() != 0) {
                    sendErrorResponse(httpExchange, 409, ALREADY_EXIST);
                    break;
                }

                Chatroom addedChatRoom = dao.addChatRoom(chatroom.getChatroomname(), chatroom.getUserid());
                dao.joinChatroom(chatroom.getUserid(), addedChatRoom.getChatroomid());
                String response = MappingUtils.makeBodyFromChatroom(addedChatRoom).toJSONString();
                sendResponse(httpExchange, response);
                break;
            case HttpMethod.GET:
                User user = parseBodyToUser(httpExchange.getRequestBody());
                List<Chatroom> chatrooms = dao.getChatRoomByUserid(user.getUserid());
                response = MappingUtils.makeBodyFromChatrooms(chatrooms).toJSONString();
                sendResponse(httpExchange, response);
                break;
            default:
                break;
        }
    }
    
    public Chatroom parseBodyToChatroom(InputStream requestBody) {
        Chatroom chatroom = new Chatroom();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJsonFromBody(requestBody));
            chatroom.setUserid(Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));
            chatroom.setChatroomname((String) jsonObject.get("chatroomname"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return chatroom;
    }

    public String getJsonFromBody(InputStream requestBody) {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = null;
        try {
            inputStreamReader = new InputStreamReader(requestBody, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            stringBuilder = new StringBuilder();
            String tempStr = "";

            while ((tempStr = bufferedReader.readLine()) != null) {
                stringBuilder.append(tempStr);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    private void sendErrorResponse(HttpExchange httpExchange, int statusCode, String detail) {
        OutputStream outputStream = null;
        try {
            httpExchange.sendResponseHeaders(statusCode, detail.length());
            outputStream = httpExchange.getResponseBody();
            outputStream.write(detail.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private User parseBodyToUser(InputStream requestBody) {
        User user = new User();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJsonFromBody(requestBody));
            user.setUserid(Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
    }

    private void sendResponse(HttpExchange httpExchange, String response) {
        OutputStream outputStream = null;
        try {
            httpExchange.sendResponseHeaders(200, response.length());
            outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
