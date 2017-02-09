package com.nexon.apiserver.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chan8 on 2017-02-09.
 */
public class ChatHandler implements HttpHandler {

    public ChatHandler() {
    }

    @Override
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

    private void handleUri(HttpExchange httpExchange) {
        String request = httpExchange.getRequestMethod();
    }

    private void handlePathVariableUri(HttpExchange httpExchange, String pathVariable) {
        String request = httpExchange.getRequestMethod();
        String response = "";
        String[] pathVariables = pathVariable.split("/");
    }
}
