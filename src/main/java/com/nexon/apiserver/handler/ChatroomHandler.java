package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Created by chan8 on 2017-02-07.
 */
public class ChatroomHandler implements HttpHandler {

    private Dao dao;

    public ChatroomHandler(Dao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
