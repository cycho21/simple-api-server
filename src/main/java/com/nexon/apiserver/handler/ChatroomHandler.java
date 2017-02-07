package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.dao.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chan8 on 2017-02-07.
 */
public class ChatroomHandler implements HttpHandler {
    private static final String ALREADY_EXIST = "Request name is already exists.";

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

    private void handlePathVariableUri(HttpExchange httpExchange, String group) {
        String requestMethod = httpExchange.getRequestMethod();
        String response = "";
        
        switch () {
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
                String response = makeBodyFromChatroom(addedChatRoom).toJSONString();
                sendResponse(httpExchange, response);
                break;
            case HttpMethod.GET:
                User user = parseBodyToUser(httpExchange.getRequestBody());
                List<Chatroom> chatroomList = dao.getChatRoomByUserid(user.getUserid());
                response = makeBodyFromChatrooms(chatroomList).toJSONString();
                sendResponse(httpExchange, response);
                break;
            default:
                break;
        }
    }

    private JSONArray makeBodyFromChatrooms(List<Chatroom> chatroomList) {
        JSONArray jsonArray = new JSONArray();
        
        for (int i = 0; i < chatroomList.size(); ++i) {
            Chatroom tempChatroom = chatroomList.get(i);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("chatroomname", tempChatroom.getChatroomname());
            jsonObject.put("chatroomid", tempChatroom.getChatroomid());
            jsonArray.add(i, jsonObject);
        }
        return jsonArray;
    }

    private JSONObject makeBodyFromChatroom(Chatroom chatroom) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chatroomid", chatroom.getChatroomid());
        jsonObject.put("chatroomname", chatroom.getChatroomname());
        jsonObject.put("userid", chatroom.getUserid());
        return jsonObject;
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
