package com.nexon.apiserver;

import com.nexon.apiserver.dao.User;

/**
 * Created by chan8 on 2017-02-06.
 */
public class Response {
    
    private int statusCode;
    private User user;

    public Response() {
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
}
