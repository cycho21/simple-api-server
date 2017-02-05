package com.nexon.apiserver.dao;

import javax.xml.transform.Result;
import java.sql.*;

/**
 * Created by Administrator on 2017-02-04.
 */
public class SimpleSqliteTemplate {

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

    public User executeQuery(String query) {
        openDb();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            System.out.println("Execute Query Success : " + query);
            User retUser = new User(null ,rs.getInt("rowid"));
            return retUser;
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

//    public String executeQuery(int userid) {
//        openDb();
//        ResultSet rs = null;
//        PreparedStatement preparedStatement = null;
//        try {
//            preparedStatement = connection.prepareStatement(query);
//            rs = preparedStatement.executeQuery();
//            System.out.println("Execute Query Success : " + query);
//            return rs.getInt("rowid");
//        } catch (SQLException e) {
//            return -1;
//        } finally {
//            try {
//                if (rs != null)
//                    rs.close();
//                if (preparedStatement != null)
//                    preparedStatement.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void openDb() {
        try {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:c:/sql/test0.db");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
