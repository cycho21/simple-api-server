package com.nexon.apiserver.utils;

import com.nexon.apiserver.dao.Chatroom;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by chan8 on 2017-02-07.
 */
public class MappingUtils {
    
    public static synchronized JSONArray makeBodyFromChatrooms(List<Chatroom> chatroomList) {
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

}
