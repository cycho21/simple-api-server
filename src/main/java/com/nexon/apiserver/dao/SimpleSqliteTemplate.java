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

    public int executeQuery(String query) {
        openDb();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            System.out.println("Execute Query Success : " + query);
            return rs.getInt("rowid");
        } catch (SQLException e) {
            return -1;
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
                connection = DriverManager.getConnection("jdbc:sqlite:c:/sql/test2.db");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
