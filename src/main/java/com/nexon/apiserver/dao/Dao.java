package com.nexon.apiserver.dao;

import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Dao {
    private SimpleSqliteTemplate simpleTemplate;
    private Logger logger = Logger.getLogger(Dao.class);

    public Dao() {
    }

    public void initialize() {
        this.simpleTemplate = new SimpleSqliteTemplate();
        dropUsersTable();
        dropChatroomTable();
        dropChatroomSnapShotTable();
        dropChatTable();
        createUsersTable();
        createChatroomTable();
        createChatroomSnapShotTable();
        createChatTable();
        logger.info("Data Access Object initialized...");
    }


    private void dropChatTable() {
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement("DROP TABLE IF EXISTS messages;"));
    }

    private void dropChatroomSnapShotTable() {
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement("DROP TABLE IF EXISTS chatroomssnapshot;"));
    }

    private void dropChatroomTable() {
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement("DROP TABLE IF EXISTS chatrooms;"));
    }

    public void dropUsersTable() {
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement("DROP TABLE IF EXISTS users"));
    }

    private void createChatTable() {
        String query = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS messages (messageid INTEGER PRIMARY KEY, ")
                .append("chatroomid INTEGER,")
                .append("senderid INTEGER,")
                .append("receiverid INTEGER,")
                .append("messagebody VARCHAR(100) not NULL);").toString();
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement(query));
    }

    private void createChatroomSnapShotTable() {
        String query = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS chatroomssnapshot ")
                .append("(userid INTEGER,")
                .append("chatroomid INTEGER);")
                .toString();
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement(query));
    }

    public void createUsersTable() {
        String query = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS users ")
                .append("(userid INTEGER PRIMARY KEY,")
                .append("nickname VARACHAR(20) not NULL);").toString();
        simpleTemplate.executeUpdate(simpleTemplate.preparedStatement(query));
    }

    public void createChatroomTable() {
        String query = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS chatrooms ")
                .append("(chatroomid INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append("userid INTEGER,")
                .append("chatroomname VARCHAR(100) not NULL);").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        simpleTemplate.executeUpdate(preparedStatement);
    }

    public int addUser(String nickname) {
        if (getUser(nickname).getUserid() != 0) {
            System.out.println("Nickname exists!");
            return -1;
        }

        String query = new StringBuilder()
                .append("INSERT INTO users ")
                .append("(nickname) values (?);").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        int userid = -1;
        try {
            preparedStatement.setString(1, nickname);
            userid = simpleTemplate.executeUpdate(preparedStatement);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        return userid;
    }

    public void joinChatroom(int userid, int chatroomid) {
        String query = new StringBuilder()
                .append("INSERT INTO chatroomssnapshot ")
                .append("(userid, chatroomid) values(?, ?);").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, userid);
            preparedStatement.setInt(2, chatroomid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }
        simpleTemplate.executeUpdate(preparedStatement);

    }

    public User getUser(String nickname) {
        String query = "SELECT userid FROM users WHERE nickname=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setString(1, nickname);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        User user = (User) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.USER);
        user.setNickname(nickname);
        return user;
    }

    public int postMessage(int senderid, int receiverid, int chatroomid, String messageBody) {
        String query = new StringBuilder()
                .append("INSERT INTO messages ")
                .append("(senderid, receiverid, chatroomid, messagebody) values(?, ?, ?, ?);").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, senderid);
            preparedStatement.setInt(2, receiverid);
            preparedStatement.setInt(3, chatroomid);
            preparedStatement.setString(4, messageBody);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int messageid = simpleTemplate.executeUpdate(preparedStatement);
        return messageid;
    }

    public Message checkMessage(int messageid) {
        Message message = new Message();
        String query = new StringBuilder()
                .append("SELECT senderid, receiverid, chatroomid, messagebody ")
                .append("FROM messages ")
                .append("WHERE messageid=?;").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, messageid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        message = (Message) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.CHAT);
        message.setMessageid(messageid);
        return message;
    }

    public ArrayList<Message> getMessagesByUserId(int chatroomid, int userid) {
        ArrayList<Message> messageList;
        String query = new StringBuilder()
                .append("SELECT senderid, receiverid, messageid, messagebody ")
                .append("FROM messages ")
                .append("WHERE chatroomid=? AND (senderid=? OR receiverid=?);").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, chatroomid);
            preparedStatement.setInt(2, userid);
            preparedStatement.setInt(3, userid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        messageList = (ArrayList<Message>) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.CHATS);
        return messageList;
    }

    public User getUser(int userid) {
        String query = "SELECT nickname FROM users WHERE userid=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, userid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        User user = (User) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.USER);
        if (user.getNickname() != null)
            user.setUserid(userid);
        return user;
    }

    public User updateUser(int userid, String nickname) {
        String query = "UPDATE users SET nickname=? WHERE userid=?;";
        if (getUser(userid).getUserid() != 0) {
            PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
            try {
                preparedStatement.setString(1, nickname);
                preparedStatement.setInt(2, userid);
                simpleTemplate.executeUpdate(preparedStatement);
            } catch (SQLException e) {
                logger.error("Error while setPreparedStatement binding variable.");
            }
        } else {
            return null;
        }

        User user = getUser(nickname);
        return user;
    }

    public void deleteUser(int userid) {
        String query = "DELETE FROM users WHERE userid=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, userid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }
        simpleTemplate.executeUpdate(preparedStatement);
    }

    public int addChatRoom(String chatroomname, int userid) {
        String query = "INSERT INTO chatrooms (chatroomname, userid) values (?, ?);";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        int chatroomid = -1;
        try {
            preparedStatement.setString(1, chatroomname);
            preparedStatement.setInt(2, userid);
            chatroomid = simpleTemplate.executeUpdate(preparedStatement);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        return chatroomid;
    }

    public Chatroom getChatRoomByNameById(String chatroomname) {
        String query = "SELECT chatroomid, userid FROM chatrooms WHERE chatroomname=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setString(1, chatroomname);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }
        Chatroom chatroom = (Chatroom) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.CHATROOM);
        chatroom.setChatroomname(chatroomname);
        return chatroom;
    }

    public Chatroom getChatRoomByNameById(int chatroomid) {
        String query = "SELECT chatroomname, userid FROM chatrooms WHERE chatroomid=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, chatroomid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }
        Chatroom chatroom = (Chatroom) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.CHATROOM);
        chatroom.setChatroomid(chatroomid);
        return chatroom;
    }

    public List<Chatroom> getChatRoomByUserid(int userid) {
        String query = new StringBuilder()
                .append("SELECT chatrooms.chatroomid, chatrooms.chatroomname ")
                .append("FROM chatrooms INNER JOIN chatroomssnapshot ")
                .append("ON chatroomssnapshot.chatroomid = chatrooms.chatroomid ")
                .append("WHERE chatroomssnapshot.userid=?;").toString();
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, userid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        List<Chatroom> chatroomList = (List<Chatroom>) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.CHATROOMS);
        return chatroomList;
    }

    public int updateChatroom(String chatroomname, int userid) {
        String query = "UPDATE chatrooms SET chatroomname=? WHERE userid=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        int chatroomid = -1;
        try {
            preparedStatement.setString(1, chatroomname);
            preparedStatement.setInt(2, userid);
            chatroomid = simpleTemplate.executeUpdate(preparedStatement);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        return chatroomid;
    }

    public void quitChatroom(int chatroomid, int userid) {
        String query = "DELETE FROM chatroomssnapshot WHERE userid=? AND chatroomid=?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, userid);
            preparedStatement.setInt(2, chatroomid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }
        simpleTemplate.executeUpdate(preparedStatement);
    }

    public ArrayList<User> getChatroomJoiner(int chatroomid) {
        String query = "SELECT userid FROM chatroomssnapshot WHERE chatroomid =?;";
        PreparedStatement preparedStatement = simpleTemplate.preparedStatement(query);
        try {
            preparedStatement.setInt(1, chatroomid);
        } catch (SQLException e) {
            logger.error("Error while setPreparedStatement binding variable.");
        }

        ArrayList<User> users = (ArrayList<User>) simpleTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.CHATROOMUSER);

        for (User us : users) {
            us.setNickname(getUser(us.getUserid()).getNickname());
        }

        return users;
    }

}
