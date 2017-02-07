package com.nexon.apiserver.dao;

/**
 * Created by chan8 on 2017-02-07.
 */
public class Chatroom {
    private int chatroomid;
    private int userid;
    private String chatroomname;

    public Chatroom(String chatroomname, int chatroomid, int userid) {
        this.chatroomid = chatroomid;
        this.userid = userid;
        this.chatroomname = chatroomname;
    }

    public Chatroom() {
    }

    public int getChatroomid() {
        return chatroomid;
    }

    public void setChatroomid(int chatroomid) {
        this.chatroomid = chatroomid;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getChatroomname() {
        return chatroomname;
    }

    public void setChatroomname(String chatroomname) {
        this.chatroomname = chatroomname;
    }
}
