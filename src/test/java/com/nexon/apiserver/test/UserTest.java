package com.nexon.apiserver.test;

import com.nexon.apiserver.Response;
import com.nexon.apiserver.Server;
import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by chan8 on 2017-02-06.
 */
public class UserTest {

    private static final String HOST_URL = "http://localhost:";
    private static final int PORT = 8000;
    private static final String BASE_URL = "/api/v1/";
    private JSONParser jsonParser;
    private RandomStringGenerator randomStringGenerator;
    private Random random;

    @Before
    public void startServer() {
        Server server = new Server();
        server.start(PORT);
        this.jsonParser = new JSONParser();
        this.randomStringGenerator = new RandomStringGenerator();
        this.random = new Random();
        randomStringGenerator.initialize();
    }

    @Test   // TCU-0111
    public void testPostUserLessTwentyLetter() throws IOException, ParseException {
        testPostUser(randomStringGenerator.nextRandomString(random.nextInt(19)));
    }

    @Test   // TCU-0112
    public void testPostUserTwentyLetters() throws IOException, ParseException {
        testPostUser(randomStringGenerator.nextRandomString(20));
    }

    @Test   // TCU-0113
    public void testPostUserOverTwentyLetter() throws IOException, ParseException {
        testPostUser(randomStringGenerator.nextRandomString(25));
    }
    
    @Test   // TCU-0114
    public void testPostUserWithSpecialLetter() throws IOException, ParseException {
        Response response = testPostUser(randomStringGenerator.nextRandomString(10) + "!@#$");
        assertEquals(400, response.getStatusCode());
    }
    
    @Test   // TCU-0121
    public void testPostUserExist() throws IOException, ParseException {
        String name = randomStringGenerator.nextRandomString(20);
        testPostUserExist(name);
    }
    
    @Test   // TCU-0211
    public void testPutUser() throws IOException, ParseException {
        String newName = randomStringGenerator.nextRandomString(20);
        String origin = randomStringGenerator.nextRandomString(20);
        Response response = testPutUser(origin, newName);
        assertEquals(response.getUser().getNickname(), newName);
    }
    
    @Test   // TCU-0212
    public void testPutUserExist() throws IOException, ParseException {
        String name = randomStringGenerator.nextRandomString(20);
        Response response = testPutUser(name, name);
        assertEquals(409, response.getStatusCode());
    }
    
    @Test   // TCU-0311
    public void testGetUser() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(20));
        int userid = response.getUser().getUserid();
        int matcheruserid = getUsers(userid).getUser().getUserid();
        assertEquals(userid, matcheruserid);
    }
    
    @Test   // TCU-0312
    public void getNonExistUser() throws IOException, ParseException {
        Response response = getUsers(randomStringGenerator.nextRandomInt());
        assertEquals(404, response.getStatusCode());
    }    

    @Test   // TCU-0411
    public void testDeleteUser() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        response = deleteUser(getUser.getUserid());

        // Delete exist user : expect status code 200
        assertEquals(200, response.getStatusCode());

        // Delete non exist user : expect status code 400
        response = deleteUser(random.nextInt(Integer.MAX_VALUE));
        assertEquals(400, response.getStatusCode());
    }
    
    @Test   // TCG-0111
    public void postChatroomExist() throws IOException, ParseException {
        postChatroomExist(randomStringGenerator.nextRandomString(50));
    }
    
    @Test   
    public void postChatroom() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(15));
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        
        response = postChatRoom(randomStringGenerator.nextRandomString(50), getUser.getUserid());
        assertEquals(response.getStatusCode(), 200);
    }
    
    @Test   // TCG-0311
    public void postChatroomWith100char() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        
        response = postChatRoom(randomStringGenerator.nextRandomString(150), getUser.getUserid());
        assertEquals(response.getChatroom().getChatroomname().length(), 100);
    }
    
    @Test   // Base of TCG-0321
    public void putChatroom() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        String newChatroomName = randomStringGenerator.nextRandomString(80);
        
        response = postChatRoom(randomStringGenerator.nextRandomString(80), getUser.getUserid());
        response = putChatroom(newChatroomName, getUser.getUserid(), response.getChatroom().getChatroomid());
        
        assertEquals(response.getChatroom().getChatroomname(), newChatroomName);
    }
    
    @Test   // TCG-0321
    public void putOthersChatroom() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();
        
        String newChatroomName = randomStringGenerator.nextRandomString(80);

        response1 = postChatRoom(randomStringGenerator.nextRandomString(80), getUser1.getUserid());
        response2 = putChatroom(newChatroomName, getUser2.getUserid(), response1.getChatroom().getChatroomid());

        assertEquals(response2.getStatusCode(), 403);
    }
    
    @Test   // TCG-0331
    public void putChatroomExist() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        String newChatroomName = randomStringGenerator.nextRandomString(80);

        response = postChatRoom(newChatroomName, getUser.getUserid());
        response = putChatroom(newChatroomName, getUser.getUserid(), response.getChatroom().getChatroomid());
        
        assertEquals(response.getStatusCode(), 409);
    }
    
    @Test // Base of TCG-0511
    public void joinRoom() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();

        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();
        
        Response response3 = postChatRoom(randomStringGenerator.nextRandomString(20), getUser2.getUserid());
        
        Response response = joinRoom(getUser1.getUserid(), response3.getChatroom().getChatroomid());response3.getChatroom().getChatroomid();
        
        assertEquals(response.getStatusCode(), 200);
    }
    
    @Test   // TCG-0511
    public void joinOneMoreRoom() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();

        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();
        
        Response response3 = postChatRoom(randomStringGenerator.nextRandomString(20), getUser2.getUserid());
        Response response4 = postChatRoom(randomStringGenerator.nextRandomString(20), getUser2.getUserid());
        
        joinRoom(getUser1.getUserid(), response4.getChatroom().getChatroomid());
        joinRoom(getUser1.getUserid(), response3.getChatroom().getChatroomid());
     
        Response response5 = getOwnChatrooms(getUser1.getUserid());

        assertEquals(response3.getChatroom().getChatroomid(), response5.getChatroomArrayList().get(1).getChatroomid());
        assertEquals(response4.getChatroom().getChatroomid(), response5.getChatroomArrayList().get(0).getChatroomid());
    }
    
    @Test   // TCU-0511
    public void getOwnChatrooms() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(15));
        Chatroom chatroom1 = postChatRoom(randomStringGenerator.nextRandomString(30), response.getUser().getUserid()).getChatroom();
        Chatroom chatroom2 = postChatRoom(randomStringGenerator.nextRandomString(30), response.getUser().getUserid()).getChatroom();
        
        response = getOwnChatrooms(response.getUser().getUserid());
        
        int id1 = response.getChatroomArrayList().get(0).getChatroomid();
        int id2 = response.getChatroomArrayList().get(1).getChatroomid();
        assertEquals(chatroom1.getChatroomid(), id1);
        assertEquals(chatroom2.getChatroomid(), id2);
    }
    
    @Test   // TCG-0611
    public void quitFromChatroom() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();

        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();

        Response response3 = postChatRoom(randomStringGenerator.nextRandomString(20), getUser1.getUserid());

        joinRoom(getUser2.getUserid(), response3.getChatroom().getChatroomid());
        Response quitResponse = quitFromChatroom(response3.getChatroom().getChatroomid(), getUser2.getUserid());
        
        assertEquals(quitResponse.getStatusCode(), 200);
        
        Response response4 = getOwnChatrooms(getUser2.getUserid());

        assertEquals(response4.getChatroomArrayList().size(), 0);
    }
    
    @Test   // TCG-0711
    public void getJoinerFromSpecificChatroom() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();

        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();
        
        Response response3 = postChatRoom(randomStringGenerator.nextRandomString(70), getUser1.getUserid());
        
        joinRoom(getUser2.getUserid(), response3.getChatroom().getChatroomid());
        
        Response response4 = getJoinerFromSpecificChatroom(response3.getChatroom().getChatroomid());
        ArrayList<User> users = response4.getUserArrayList();
        
        assertEquals(users.get(1).getUserid(), getUser2.getUserid());
    }
    
    @Test   // TCN-0111
    public void postMessage() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();
        
    }

    private Response getJoinerFromSpecificChatroom(int chatroomid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "chatrooms/" + chatroomid + "/users";

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");

        Response response = new Response();

        if (urlConnection.getResponseCode() != 200) {
            response.setStatusCode(urlConnection.getResponseCode());
            return response;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        urlConnection.disconnect();
        JSONObject obj = (JSONObject) jsonParser.parse(sb.toString());

        System.out.println(sb.toString());
        JSONArray jsonArray = (JSONArray) obj.get("users");
        ArrayList<User> userList = makeArrayUserListFromJsonArray(jsonArray);
        response.setUserArrayList(userList);
        return response;
    }


    public Response quitFromChatroom(int chatroomid, int userid) throws IOException {
        String str = HOST_URL + PORT + BASE_URL + "chatrooms/" + chatroomid + "/users/" + userid;

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        urlConnection.setRequestProperty("Accept", "application/json");

        int statusCode = urlConnection.getResponseCode();
        Response response = new Response();
        response.setStatusCode(statusCode);

        return response;
    }
    
    
    private Response joinRoom(int userid, int chatroomid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "chatrooms/" + chatroomid + "/users";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chatroomid", chatroomid);
        jsonObject.put("userid", userid);

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream out = urlConnection.getOutputStream();
        out.write(jsonObject.toJSONString().getBytes());
        out.flush();

        int statusCode = urlConnection.getResponseCode();

        if (statusCode != 200) {
            return new Response(statusCode);
        }

        
        return new Response(statusCode);
    }

    
    private Response putChatroom(String chatroomname, int userid, int chatroomid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "chatrooms/" + chatroomid;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chatroomname", chatroomname);
        jsonObject.put("userid", userid);

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream out = urlConnection.getOutputStream();
        out.write(jsonObject.toJSONString().getBytes());
        out.flush();

        int statusCode = urlConnection.getResponseCode();
        if (statusCode != 200) {
            return new Response(statusCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        Response response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        System.out.println(sb.toString());
        response.getChatroom().setChatroomname((String) jsonObject.get("chatroomname"));
        response.getChatroom().setUserid(Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));
        response.getChatroom().setChatroomid(Integer.parseInt(String.valueOf(jsonObject.get("chatroomid")), 10));
        return response;
    }

    private Response getOwnChatrooms(int userid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "users/" + userid + "/chatrooms";
        
        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");

        Response response = new Response();
        
        if (urlConnection.getResponseCode() != 200) {
            response.setStatusCode(urlConnection.getResponseCode());
            return response;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        urlConnection.disconnect();
        JSONObject obj = (JSONObject) jsonParser.parse(sb.toString());
        JSONArray jsonArray = (JSONArray) obj.get("chatrooms");
        ArrayList<Chatroom> chatroomList = makeArrayListFromJsonArray(jsonArray);
        response.setChatroomArrayList(chatroomList);
        return response;
    }

    private ArrayList<User> makeArrayUserListFromJsonArray(JSONArray jsonArray) {
        ArrayList<User> userArrayList = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            User user = new User();
            user.setUserid(Integer.parseInt(String.valueOf(obj.get("userid"))));
            user.setNickname((String) obj.get("nickname"));
            
            userArrayList.add(user);
        }
        return userArrayList;
    }

    private ArrayList<Chatroom> makeArrayListFromJsonArray(JSONArray jsonArray) {
        ArrayList<Chatroom> chatroomArrayList = new ArrayList<Chatroom>();
        
        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            Chatroom chatroom = new Chatroom();
            chatroom.setChatroomname((String) obj.get("chatroomname"));
            chatroom.setChatroomid(Integer.parseInt(String.valueOf(obj.get("chatroomid"))));
            
            chatroomArrayList.add(chatroom);
        }
        
        return chatroomArrayList;
    }

    private void postChatroomExist(String chatroomname) throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(15));
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        String randString = randomStringGenerator.nextRandomString(50); 
        postChatRoom(randString, getUser.getUserid());
        response = postChatRoom(randString, getUser.getUserid());
        assertEquals(response.getStatusCode(), 409);
    }
    
    private Response postChatRoom(String chatroomname, int userid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "chatrooms/";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chatroomname", chatroomname);
        jsonObject.put("userid", userid);

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream out = urlConnection.getOutputStream();
        out.write(jsonObject.toJSONString().getBytes());
        out.flush();
        
        int statusCode = urlConnection.getResponseCode();

        if (statusCode != 200) {
            return new Response(statusCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        Response response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        response.getChatroom().setChatroomid(Integer.parseInt(String.valueOf(jsonObject.get("chatroomid")), 10));
        response.getChatroom().setUserid(Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));
        response.getChatroom().setChatroomname((String) jsonObject.get("chatroomname"));

        return response;
    }
    

    private void testPostUserExist(String name) throws IOException, ParseException {
        Response response = postUsers(name);
        
        response = postUsers(name);
        assertEquals(response.getStatusCode(), 409);
    }

    private Response deleteUser(int userid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "users/" + userid;

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("DELETE");
        urlConnection.setRequestProperty("Accept", "application/json");

        Response response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        return response;
    }

    public Response testPostUser(String nickname) throws IOException, ParseException {
        Response response = postUsers(nickname);
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        assertEquals(response.getUser().getNickname(), getUser.getNickname());
        return response;
    }

    public Response testPutUser(String originName, String newName) throws IOException, ParseException {
        Response response = postUsers(originName);
        User getUser = getUsers(response.getUser().getUserid()).getUser();

        response = putUser(getUser.getUserid(), newName);

        return response;
    }

    private Response putUser(int userid, String nickname) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "users/" + userid;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", nickname);

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream out = urlConnection.getOutputStream();
        out.write(jsonObject.toJSONString().getBytes());
        out.flush();

        int statusCode = urlConnection.getResponseCode();
        if (statusCode != 200) {
            return new Response(statusCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        Response response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        response.getUser().setNickname((String) jsonObject.get("nickname"));
        response.getUser().setUserid(Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));

        return response;
    }

    public Response postUsers(String nickname) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "users/";
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", nickname);

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream out = urlConnection.getOutputStream();
        out.write(jsonObject.toJSONString().getBytes());
        out.flush();

        int statusCode = urlConnection.getResponseCode();
        if (statusCode != 200) {
            return new Response(statusCode);
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        Response response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        response.getUser().setNickname((String) jsonObject.get("nickname"));
        response.getUser().setUserid(Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));

        return response;
    }

    public Response getUsers(int userid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "users/" + userid;

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");

        Response response = new Response();

        if (urlConnection.getResponseCode() != 200) {
            response.setStatusCode(urlConnection.getResponseCode());
            return response; 
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        urlConnection.disconnect();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        User user = new User((String) jsonObject.get("nickname"), Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));
        response.setUser(user);
        return response;
    }
}