package com.nexon.apiserver.dao;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-02-04.
 */
public class SimpleSqliteTemplate {
    public static final int USER = 0;
    public static final int CHATROOM = 1;
    public static final int CHATROOMS = 2;
    public static final int CHAT = 3;
    public static final int CHATROOMUSER = 4;
    public static final int LAST_INSERT_ID = 5;

    private Connection connection;
    private String testFileName;

    public SimpleSqliteTemplate() {
        this.testFileName = "test.db";
    }

    public Connection makeConnection() {
        openDb();
        return this.connection;
    }

    public int executeUpdate(PreparedStatement preparedStatement) {
        int last_insert_rowid = -1;
        
        try {
            preparedStatement.executeUpdate();
            last_insert_rowid = preparedStatement.getGeneratedKeys().getInt("last_insert_rowid()");
//            System.out.println(" :: Execute Update Query Success :: ");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!preparedStatement.isClosed() && preparedStatement != null)
                    preparedStatement.close();
                if (!connection.isClosed() && connection != null)
                    connection.close();
            } catch (SQLException e) {
            }
        }
        return last_insert_rowid;
    }

    public Object resultSetToObject(ResultSet resultSet, int type) {
        switch (type) {
            case USER:
                User user = null;
                try {
                    user = new User(null, resultSet.getInt("userid"));
                    return user;
                } catch (SQLException e) {
                    try {
                        user = new User(resultSet.getString("nickname"), 0);
                        return user;
                    } catch (SQLException e1) {
                        return new User(null, 0);
                    }
                }
            case CHATROOM:
                Chatroom chatroom = null;
                try {
                    chatroom = new Chatroom(null, resultSet.getInt("chatroomid"), resultSet.getInt("userid"));
                    return chatroom;
                } catch (SQLException e) {
                    try {
                        chatroom = new Chatroom(resultSet.getString("chatroomname"), 0, resultSet.getInt("userid"));
                        return chatroom;
                    } catch (SQLException e1) {
                        return new Chatroom(null, 0, 0);
                    }
                }
            case CHATROOMS:
                List<Chatroom> chatroomList = new ArrayList<Chatroom>();
                try {
                    while (resultSet.next()) {
                        Chatroom tempchatroom = new Chatroom();
                        tempchatroom.setChatroomid(resultSet.getInt("chatroomid"));
                        tempchatroom.setChatroomname(resultSet.getString("chatroomname"));
                        chatroomList.add(tempchatroom);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return chatroomList;
            case CHATROOMUSER:
                ArrayList<User> userList = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        user = new User();
                        user.setUserid(resultSet.getInt("userid"));
                        userList.add(user);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return userList;
            case CHAT:
                break;
            case LAST_INSERT_ID:
                int lastid = -1;
                try {
                    System.out.println(resultSet.getMetaData().getColumnCount());
                    System.out.println(resultSet.getMetaData().getColumnName(1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return lastid;
        }
        return null;
    }

    public Object executeQuery(PreparedStatement preparedStatement, int type) {
        openDb();
        ResultSet rs = null;
        try {
            rs = preparedStatement.executeQuery();
//            System.out.println(" :: Execute Query Success :: ");
            return resultSetToObject(rs, type);
        } catch (SQLException e) {
            return new User(null, 0);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (preparedStatement != null)
                    preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void openDb() {
        try {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:./" + testFileName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PreparedStatement preparedStatement(String preparedQuery) {
        openDb();
        try {
            System.out.println(preparedQuery);
            PreparedStatement preparedStatement = this.makeConnection().prepareStatement(preparedQuery);
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}