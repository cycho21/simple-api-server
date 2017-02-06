package com.nexon.apiserver.dao;

import java.sql.*;

/**
 * Created by Administrator on 2017-02-04.
 */
public class SimpleSqliteTemplate {
    public static final int USER = 0;
    public static final int CHATROOM = 1;
    public static final int CHAT = 2;

    private Connection connection;
    private String testFileName;

    public SimpleSqliteTemplate() {
        this.testFileName = "test.db";
    }

    public Connection makeConnection() {
        openDb();
        return this.connection;
    }

    public void executeUpdate(PreparedStatement preparedStatement) {
        try {
            preparedStatement.executeUpdate();
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
                e.printStackTrace();
            }
        }
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
                break;
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
            PreparedStatement preparedStatement = this.makeConnection().prepareStatement(preparedQuery);
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}