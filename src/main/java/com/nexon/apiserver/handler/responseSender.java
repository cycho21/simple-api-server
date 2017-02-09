package com.nexon.apiserver.handler;

import com.sun.net.httpserver.HttpExchange;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan8 on 2017-02-07.
 */
public class ResponseSender {
    
    private Logger logger = Logger.getLogger(ResponseSender.class);

    public void sendResponse(HttpExchange httpExchange, String response) {
        OutputStream outputStream = null;
        try {
            httpExchange.sendResponseHeaders(200, response.length());
            outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
        } catch (IOException e) {
            logger.error(response);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("OutputStream close error");
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
            logger.error(detail);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("OutputStream close error");
            }
        }
    }
}
