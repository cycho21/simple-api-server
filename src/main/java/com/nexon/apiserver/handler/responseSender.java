package com.nexon.apiserver.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan8 on 2017-02-07.
 */
public class ResponseSender {

    public void sendResponse(HttpExchange httpExchange, String response) {
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

    public void sendErrorResponse(HttpExchange httpExchange, int statusCode, String detail) {
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
}
