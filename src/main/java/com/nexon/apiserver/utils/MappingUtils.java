package com.nexon.apiserver.utils;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chan8 on 2017-02-07.
 */
public class MappingUtils {

    public static synchronized JSONObject makeBodyFromChatrooms(List<Chatroom> chatroomList) {
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

    public static synchronized JSONObject makeBodyFromChatroom(Chatroom chatroom) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chatroomid", chatroom.getChatroomid());
        jsonObject.put("chatroomname", chatroom.getChatroomname());
        jsonObject.put("userid", chatroom.getUserid());
        return jsonObject;
    }

    public static synchronized JSONObject makeBodyFromUser(User retUser) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", retUser.getNickname());
        jsonObject.put("userid", retUser.getUserid());
        return jsonObject;
    }
    
    public static synchronized JSONObject makeBodyFromUsers(ArrayList<User> users) {
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
}
