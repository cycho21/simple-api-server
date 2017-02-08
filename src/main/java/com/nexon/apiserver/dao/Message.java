package com.nexon.apiserver.dao;

/**
 * Created by chan8 on 2017-02-08.
 */
public class Message {
    private int senderid;
    private int receiverid;
    private int messageid;
    private String messageBody;
    private int chatroomid;

    public Message(int senderid, int receiverid, int messageid, int chatroomid, String messageBody) {
        this.senderid = senderid;
        this.receiverid = receiverid;
        this.messageid = messageid;
        this.messageBody = messageBody;
        this.chatroomid = chatroomid;
    }
    
    public Message() {
    }

    public int getSenderid() {
        return senderid;
    }

    public void setSenderid(int senderid) {
        this.senderid = senderid;
    }

    public int getReceiverid() {
        return receiverid;
    }

    public void setReceiverid(int receiverid) {
        this.receiverid = receiverid;
    }

    public int getMessageid() {
        return messageid;
    }

    public void setMessageid(int messageid) {
        this.messageid = messageid;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public int getChatroomid() {
        return chatroomid;
    }

    public void setChatroomid(int chatroomid) {
        this.chatroomid = chatroomid;
    }
}
