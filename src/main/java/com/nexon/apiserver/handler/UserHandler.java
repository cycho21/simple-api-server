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
    private static final String SPECIAL_LETTER = "Nickname must alphanumeric but request nickname contains special letters.";
    private static final String LONGER_THAN_TWENTY = "Nickname must less than 20 characters.";
    private static final String NO_USER = "There is no user that you request";
    private NicknameValidator nicknameValidator;
    private JSONParser jsonParser;
    private Dao dao;

    public UserHandler(Dao dao) {
        this.dao = dao;
        this.jsonParser = new JSONParser();
        this.nicknameValidator = new NicknameValidator();
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
        String response = "";
        User user;
        switch (request) {
            case HttpMethod.GET:
                user = dao.getUser(Integer.parseInt(pathVariable, 10));
                response = makeBodyFromUser(user).toJSONString();
                sendResponse(httpExchange, response);
                break;
            case HttpMethod.PUT:
                user = parseBodyToUser(httpExchange.getRequestBody());
                switch (nicknameValidator.isValidateName(user.getNickname())) {
                    case NicknameValidator.ALPHA_NUMERIC:
                        dao.updateUser(Integer.parseInt(pathVariable, 10), user.getNickname());
                        user = dao.getUser(Integer.parseInt(pathVariable, 10));
                        response = makeBodyFromUser(user).toJSONString();
                        sendResponse(httpExchange, response);
                        break;
                    case NicknameValidator.LONGER_THAN_TWENTY:
                        sendErrorResponse(httpExchange, 400, LONGER_THAN_TWENTY);
                        break;
                    case NicknameValidator.SPECIAL_LETTER:
                        sendErrorResponse(httpExchange, 400, SPECIAL_LETTER);
                        break;
                }
                break;
            case HttpMethod.DELETE:
                if (dao.getUser(Integer.parseInt(pathVariable, 10)).getUserid() != 0) {
                    dao.deleteUser(Integer.parseInt(pathVariable, 10));
                    sendResponse(httpExchange, response);
                } else {
                    sendErrorResponse(httpExchange, 400, NO_USER);
                }
                break;
        }
    }

    private void handleUri(HttpExchange httpExchange) {
        String request = httpExchange.getRequestMethod();

        switch (request) {
            case HttpMethod.GET:
                break;
            case HttpMethod.POST:
                User user = parseBodyToUser(httpExchange.getRequestBody());
                int code = nicknameValidator.isValidateName(user.getNickname());
                switch (code) {
                    case NicknameValidator.SPECIAL_LETTER:
                        sendErrorResponse(httpExchange, 400, SPECIAL_LETTER);
                        break;
                    case NicknameValidator.LONGER_THAN_TWENTY:
                        sendErrorResponse(httpExchange, 400, LONGER_THAN_TWENTY);
                        break;
                    case NicknameValidator.ALPHA_NUMERIC:
                        User retUser = dao.addUser(user.getNickname());
                        String response = makeBodyFromUser(retUser).toJSONString();
                        sendResponse(httpExchange, response);
                        break;
                }
                break;
        }
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

    private JSONObject makeBodyFromUser(User retUser) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", retUser.getNickname());
        jsonObject.put("userid", retUser.getUserid());
        return jsonObject;
    }

    private User parseBodyToUser(InputStream requestBody) {
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
}