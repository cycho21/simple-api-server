package com.nexon.apiserver.test;

import com.nexon.apiserver.Response;
import com.nexon.apiserver.Server;
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

    @Test
    public void testPostUserLessTwentyLetter() throws IOException, ParseException {
        testPostUser(randomStringGenerator.nextRandomString(random.nextInt(19)));
    }

    @Test
    public void testPostUserTwentyLetters() throws IOException, ParseException {
        testPostUser(randomStringGenerator.nextRandomString(20));
    }

    @Test
    public void testPostUserOverTwentyLetter() throws IOException, ParseException {
        testPostUser(randomStringGenerator.nextRandomString(25));
    }
    
    @Test
    public void testPutUser() throws IOException, ParseException {
        testPutUser(randomStringGenerator.nextRandomString(20), randomStringGenerator.nextRandomString(20));
    }
    
    @Test
    public void testDeleteUser() throws IOException, ParseException {
        Response response = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser = getUsers(response.getUser().getUserid());
        response = deleteUser(getUser.getUserid());
        
        // Delete exist user : expect status code 200
        assertEquals(200, response.getStatusCode());
        
        // Delete non exist user : expect status code 400
        response = deleteUser(random.nextInt(Integer.MAX_VALUE));
        assertEquals(400, response.getStatusCode());
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
        User getUser = getUsers(response.getUser().getUserid());
        assertEquals(response.getUser().getNickname(), getUser.getNickname());
    }

    public void testPutUser(String originName, String newName) throws IOException, ParseException {
        Response response = postUsers(originName);
        User getUser = getUsers(response.getUser().getUserid());
        
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

    public User getUsers(int userid) throws IOException, ParseException {
        String str = HOST_URL + PORT + BASE_URL + "users/" + userid;

        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        urlConnection.disconnect();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        User user = new User((String) jsonObject.get("nickname"), Integer.parseInt(String.valueOf(jsonObject.get("userid")), 10));

        return user;
    }
}