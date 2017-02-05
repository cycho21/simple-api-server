package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Dao;
import com.nexon.apiserver.dao.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017-02-04.
 */
public class UserHandler implements HttpHandler {

    private JSONParser jsonParser;
    private Dao dao;

    public UserHandler(Dao dao) {
        this.dao = dao;
        this.jsonParser = new JSONParser();
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        Pattern pattern = Pattern.compile("(?<=/users/).+$");
        Matcher matcher = pattern.matcher(path);

        boolean hasPathVariable = matcher.find();

        if (hasPathVariable == true)
            handlePathVariableUri(httpExchange, matcher.group());
        else
            handleUri(httpExchange);
    }

    private void handlePathVariableUri(HttpExchange httpExchange, String pathVariable) {
        String request = httpExchange.getRequestMethod();

        switch (request) {
            case "GET":
                break;
            case "POST":
                User user = parseResponseBodyToUser(httpExchange.getRequestBody());
                System.out.println(user.getNickname());
                break;
        }
    }

    private void handleUri(HttpExchange httpExchange) {
        String request = httpExchange.getRequestMethod();

        switch (request) {
            case "GET":
                break;
            case "POST":
                User user = parseResponseBodyToUser(httpExchange.getRequestBody());
                User retUser = dao.addUser(user.getNickname());
                String response = makeBodyFromUser(retUser).toJSONString();
                sendResponce(httpExchange, response);
                break;
        }
    }

    private void sendResponce(HttpExchange httpExchange, String response) {
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

    private JSONObject makeBodyFromUser(User retUser) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", retUser.getNickname());
        jsonObject.put("userid", retUser.getUserid());
        return jsonObject;
    }

    private User parseResponseBodyToUser(InputStream requestBody) {
        User user = new User();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(getJsonFromBody(requestBody));
            user.setNickname((String) jsonObject.get("nickname"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
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

    public void setDao(Dao dao) {
        this.dao = dao;
    }
}
