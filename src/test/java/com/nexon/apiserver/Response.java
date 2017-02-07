package com.nexon.apiserver;

import com.nexon.apiserver.dao.Chatroom;
import com.nexon.apiserver.dao.User;

/**
 * Created by chan8 on 2017-02-06.
 */
public class Response {
    
    private int statusCode;
    private User user;
    private Chatroom chatroom;

    public Response() {
        this.user = new User();
        this.chatroom = new Chatroom();
    }
    
    public Response(int statusCode) {
        this.statusCode = statusCode;
        this.user = new User();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }
}
