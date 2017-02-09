package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.Message;
import com.nexon.apiserver.dao.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chan8 on 2017-02-07.
 */
public class SimpleMapper {
    private JSONParser jsonParser;

    public SimpleMapper() {
        this.jsonParser = new JSONParser();
    }


    public JSONObject makeBodyFromChatrooms(List<Chatroom> chatroomList) {
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < chatroomList.size(); ++i) {
            Chatroom tempChatroom = chatroomList.get(i);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("chatroomname", tempChatroom.getChatroomname());
            jsonObject.put("chatroomid", tempChatroom.getChatroomid());
            jsonArray.add(i, jsonObject);
        }

        jsonObj.put("chatrooms", jsonArray);
        return jsonObj;
    }

    public JSONObject makeBodyFromChatroom(Chatroom chatroom) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chatroomid", chatroom.getChatroomid());
        jsonObject.put("chatroomname", chatroom.getChatroomname());
        jsonObject.put("userid", chatroom.getUserid());
        return jsonObject;
    }

    public JSONObject makeBodyFromUser(User retUser) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", retUser.getNickname());
        jsonObject.put("userid", retUser.getUserid());
        return jsonObject;
    }

    public JSONObject makeBodyFromUsers(ArrayList<User> users) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (User user : users) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userid", user.getUserid());
            jsonObj.put("nickname", user.getNickname());
            jsonArray.add(jsonObj);
        }

        jsonObject.put("users", jsonArray);
        return jsonObject;
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

    public User parseBodyToUser(InputStream requestBody) {
        User user = new User();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJsonFromBody(requestBody));
            user.setNickname((String) jsonObject.get("nickname"));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User parseBodyToUserWithID(InputStream requestBody) {
        User user = new User();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJsonFromBody(requestBody));
            if (jsonObject.get("nickname") != null)
                user.setNickname((String) jsonObject.get("nickname"));
            if (jsonObject.get("userid") != null)
            user.setUserid(getInt(jsonObject.get("userid")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
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

    public Message parseBodyToMessage(InputStream requestBody) {
        Message message = new Message();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJsonFromBody(requestBody));
            message.setSenderid(getInt(jsonObject.get("senderid")));
            message.setReceiverid(getInt(jsonObject.get("receiverid")));
            message.setMessageBody((String) jsonObject.get("messagebody"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return message;
    }

    private int getInt(Object jsonValue) {
        return Integer.parseInt(String.valueOf(jsonValue));
    }

    public JSONObject makeBodyFromMessage(Message message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messageid", message.getMessageid());
        jsonObject.put("senderid", message.getSenderid());
        jsonObject.put("receiverid", message.getReceiverid());
        jsonObject.put("messagebody", message.getMessageBody());
        return jsonObject;
    }

    public JSONObject makeBodyFromMessages(ArrayList<Message> messageArrayList) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (Message message : messageArrayList) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("senderid", message.getSenderid());
            jsonObj.put("receiverid", message.getReceiverid());
            jsonObj.put("messagebody", message.getMessageBody());
            jsonObj.put("messageid", message.getMessageid());
            jsonArray.add(jsonObj);
        }
        
        jsonObject.put("messages", jsonArray);
        return jsonObject;
    }
}
