package com.nexon.apiserver.test;

import com.nexon.apiserver.Response;
import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.Message;
import com.nexon.apiserver.dao.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by chan8 on 2017-02-09.
 */
public class MessageTest {

    private static String HOST = "http://localhost:";
    private static String BASE_URL = "/api/v1/";
    private static int PORT = 0;
    private JSONParser jsonParser;
    private RandomStringGenerator randomStringGenerator;
    private Random random;
    private String DEST = null;

    @Before
    public void startServer() {
//        Server server = new Server();
//        server.initialize();
//        server.start();

        this.jsonParser = new JSONParser();
        this.randomStringGenerator = new RandomStringGenerator();
        this.random = new Random();
        randomStringGenerator.initialize();
        initialize();
    }

    private void initialize() {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(new FileReader("./config.json"));
        } catch (IOException e) {
            System.out.println("Error occured when parse from config.json");
        } catch (ParseException e) {
            System.out.println("Error occured when parse from config.json. Check Json syntax");
        }
        this.BASE_URL = (String) jsonObject.get("baseurl");
        this.PORT = Integer.parseInt(String.valueOf(jsonObject.get("port")));
        this.HOST = (String) jsonObject.get("host");

        this.DEST = HOST + ":" + PORT + BASE_URL;
    }

    @Test   // TCN-0111
    public void postMessage() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();
        String chatroomname = randomStringGenerator.nextRandomString(80);

        int chatroomid = postChatRoom(chatroomname, getUser1.getUserid()).getChatroom().getChatroomid();
        Message message = postMessage(getUser1.getUserid(), 0, chatroomid, randomStringGenerator.nextRandomString(100)).getMessage();

        Message message2 = getMessage(getUser1.getUserid(), chatroomid).getMessagesArrayList().get(0);
        assertEquals(message.getMessageid(), message2.getMessageid());
        assertEquals(message.getMessageBody(), message2.getMessageBody());
    }

    @Test   // TCN-0211
    public void getWhipserByReceiverOrSender() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();

        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();

        Response response3 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser3 = getUsers(response3.getUser().getUserid()).getUser();

        int chatroomid = postChatRoom(randomStringGenerator.nextRandomString(80), getUser1.getUserid()).getChatroom().getChatroomid();

        joinRoom(getUser2.getUserid(), chatroomid);
        joinRoom(getUser3.getUserid(), chatroomid);

        String msg = "THIS IS FROM USER1 TO USER2 MESSAGE";

        postMessage(getUser1.getUserid(), getUser2.getUserid(), chatroomid, msg);

        Message message1 = getMessage(getUser1.getUserid(), chatroomid).getMessagesArrayList().get(0);
        Message message2 = getMessage(getUser2.getUserid(), chatroomid).getMessagesArrayList().get(0);

        assertEquals(message1.getMessageBody(), msg);
        assertEquals(message2.getMessageBody(), msg);
        assertEquals(message1.getMessageBody(), message2.getMessageBody());
        assertEquals(message1.getMessageid(), message2.getMessageid());
    }

    @Test   // TCN-0222
    public void getWhisperByOtherUser() throws IOException, ParseException {
        Response response1 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser1 = getUsers(response1.getUser().getUserid()).getUser();

        Response response2 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser2 = getUsers(response2.getUser().getUserid()).getUser();

        Response response3 = postUsers(randomStringGenerator.nextRandomString(20));
        User getUser3 = getUsers(response3.getUser().getUserid()).getUser();

        int chatroomid = postChatRoom(randomStringGenerator.nextRandomString(80), getUser1.getUserid()).getChatroom().getChatroomid();

        joinRoom(getUser2.getUserid(), chatroomid);
        joinRoom(getUser3.getUserid(), chatroomid);

        String msg = "THIS IS FROM USER1 TO USER2 MESSAGE";

        postMessage(getUser1.getUserid(), getUser2.getUserid(), chatroomid, msg);

        ArrayList<Message> message3 = getMessage(getUser3.getUserid(), chatroomid).getMessagesArrayList();

        assertEquals(message3.size(), 0);
    }

    private Response getMessage(int userid, int chatroomid) throws IOException, ParseException {
        String str = DEST + "chatrooms/" + chatroomid + "/messages";
        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("userid", String.valueOf(userid));

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
        JSONArray jsonArray = (JSONArray) obj.get("messages");
        ArrayList<Message> messageList = makeArrayMessageListFromJsonArray(jsonArray);
        response.setMessagesArrayList(messageList);
        return response;
    }

    private ArrayList<Message> makeArrayMessageListFromJsonArray(JSONArray jsonArray) {

        ArrayList<Message> messagesArrayList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            Message message = new Message();
            message.setSenderid(getInt(obj.get("senderid")));
            message.setReceiverid(getInt(obj.get("receiverid")));
            message.setMessageid(getInt(obj.get("messageid")));
            message.setMessageBody((String) obj.get("messagebody"));
            messagesArrayList.add(message);
        }

        return messagesArrayList;
    }

    private int getInt(Object jsonValue) {
        return Integer.parseInt(String.valueOf(jsonValue));
    }


    private Response postMessage(int senderid, int receiverid, int chatroomid, String messagebody) throws IOException, ParseException {
        String str = DEST + "chatrooms/" + chatroomid + "/messages";
        JSONObject jsonObject = new JSONObject();
        /*
        
         */
        jsonObject.put("chatroomid", chatroomid);
        jsonObject.put("senderid", senderid);
        jsonObject.put("receiverid", receiverid);
        jsonObject.put("messagebody", messagebody);

        /*
        
         */
        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        /*
        
         */
        OutputStream out = urlConnection.getOutputStream();
        out.write(jsonObject.toJSONString().getBytes());
        out.flush();

        int statusCode = urlConnection.getResponseCode();

        if (statusCode != 200)
            return new Response(statusCode);

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String output;
        StringBuilder sb = new StringBuilder();

        while ((output = br.readLine()) != null)
            sb.append(output);

        Response response = new Response();
        response.setStatusCode(urlConnection.getResponseCode());

        jsonObject = (JSONObject) jsonParser.parse(sb.toString());
        response.getMessage().setMessageBody((String) jsonObject.get("messagebody"));
        response.getMessage().setMessageid(parseFromString(String.valueOf(jsonObject.get("messageid"))));
        response.getMessage().setSenderid(parseFromString(String.valueOf(jsonObject.get("senderid"))));
        response.getMessage().setReceiverid(parseFromString(String.valueOf(jsonObject.get("receiverid"))));

        return response;
    }

    public int parseFromString(String unparse) {
        return Integer.parseInt(String.valueOf(unparse));
    }

    private Response joinRoom(int userid, int chatroomid) throws IOException, ParseException {
        String str = DEST + "chatrooms/" + chatroomid + "/users";
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

    private Response postChatRoom(String chatroomname, int userid) throws IOException, ParseException {
        String str = DEST + "chatrooms/";
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

    public Response postUsers(String nickname) throws IOException, ParseException {
        String str = DEST + "users/";
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
        String str = DEST + "users/" + userid;

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