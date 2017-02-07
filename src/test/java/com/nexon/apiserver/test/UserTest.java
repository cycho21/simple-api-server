package com.nexon.apiserver.test;

import com.nexon.apiserver.Response;
import com.nexon.apiserver.Server;
import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    @Test   // TCU-0121
    public void testPostUserExist() throws IOException, ParseException {
        String name = randomStringGenerator.nextRandomString(20);
        testPostUserExist(name);
    }

    @Test   // TCU-0211
    public void testPutUser() throws IOException, ParseException {
        testPutUser(randomStringGenerator.nextRandomString(20), randomStringGenerator.nextRandomString(20));
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
    
    @Test   // TCU-0511
    public void getChatrooms() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(15));
        Chatroom chatroom1 = postChatRoom(randomStringGenerator.nextRandomString(30), response.getUser().getUserid()).getChatroom();
        Chatroom chatroom2 = postChatRoom(randomStringGenerator.nextRandomString(30), response.getUser().getUserid()).getChatroom();
        assertEquals(chatroom1.getUserid(), chatroom2.getUserid());
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

    public void testPostUser(String nickname) throws IOException, ParseException {
        Response response = postUsers(nickname);
        User getUser = getUsers(response.getUser().getUserid()).getUser();
        assertEquals(response.getUser().getNickname(), getUser.getNickname());
    }

    public void testPutUser(String originName, String newName) throws IOException, ParseException {
        Response response = postUsers(originName);
        User getUser = getUsers(response.getUser().getUserid()).getUser();

        response = putUser(getUser.getUserid(), newName);

        assertEquals(response.getUser().getNickname(), newName);
        assertEquals(response.getUser().getUserid(), getUser.getUserid());
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