package com.nexon.apiserver.dao;

import com.nexon.apiserver.test.RandomStringGenerator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-02-04.
 */
public class DaoTest {

    private Dao dao;
    private RandomStringGenerator randomStringGenerator;

    @Before
    public void setUp() {
        this.dao = new Dao();
        dao.initialize();
        this.randomStringGenerator = new RandomStringGenerator();
        randomStringGenerator.initialize();
//        dao.createUsersTable();
//        dao.dropUsersTable();
//        dao.createChatroomTable();
        System.out.println("SETUP OK");
    }

//    @Test
//    public void makeBodyFromChatrooms() {
//        JSONArray jsonArray = new JSONArray();
//        List<Chatroom> chatroomList = new ArrayList<>();
//        for (int i = 0; i < 10; ++i) {
//            Chatroom chatroom = new Chatroom();
//            chatroom.setChatroomname(randomStringGenerator.nextRandomString(20));
//            chatroom.setChatroomid(randomStringGenerator.nextRandomInt());
//            chatroomList.add(i, chatroom);
//        }
//        
//        
//        for (int i = 0; i < chatroomList.size(); ++i) {
//            Chatroom tempChatroom = chatroomList.get(i);
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("chatroomname", tempChatroom.getChatroomname());
//            jsonObject.put("chatroomid", tempChatroom.getChatroomid());
//            jsonArray.add(i, jsonObject);
//        }
//        System.out.println(jsonArray.toJSONString());
//    }

    @Test
    public void testInsertOperation() {
        dao.postMessage(1, 2, 1, "THISISTEST");
        dao.postMessage(1, 2, 1, "THISISTEST");
        dao.postMessage(1, 2, 1, "THISISTEST");
        dao.postMessage(1, 2, 1, "THISISTEST");
        dao.postMessage(1, 2, 1, "THISISTEST");
    }

    @After
    public void dropTable() {
//        dao.dropUsersTable();
    }
}