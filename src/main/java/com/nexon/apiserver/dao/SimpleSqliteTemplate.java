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

    public SimpleSqliteTemplate() {
    }

    public void executeUpdate(String query) {
        openDb();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Execute Update Query Success : " + query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!statement.isClosed() && statement != null)
                    statement.close();
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
                    user = new User(null, resultSet.getInt("rowid"));
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

    public Object executeQuery(String query, int type) {
        openDb();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            System.out.println("Execute Query Success : " + query);
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
                connection = DriverManager.getConnection("jdbc:sqlite:./test0.db");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}