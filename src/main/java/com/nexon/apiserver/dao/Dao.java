package com.nexon.apiserver.dao;

import java.sql.*;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Dao {

    private final String fileName;
    private final SimpleSqliteTemplate jdbcTemplate;
    private Connection connection;

    public Dao() {
        this.fileName = "TestFileName";
        this.jdbcTemplate = new SimpleSqliteTemplate();
    }

    public void dropTable() {
        jdbcTemplate.executeUpdate("DROP TABLE users");
    }

    public void createTable() {
        jdbcTemplate.executeUpdate("CREATE TABLE users (nickname VARACHAR(20) not NULL);");
    }

    public User addUser(String nickname) {
        jdbcTemplate.executeUpdate("INSERT INTO users values ('" + nickname + "');");
        int userid = jdbcTemplate.executeQuery("SELECT rowid FROM users WHERE nickname='" + nickname + "';");
        User retUser = new User(nickname, userid);
        return retUser;
    }

    private int getUser(String nickname) {
        int userid  = jdbcTemplate.executeQuery("SELECT rowid FROM users WHERE nickname='" + nickname + "';");
        return userid;
    }

//    public User getUser(int userid) {
//        ResultSet rs = jdbcTemplate.executeQuery("SELECT nickname FROM users WHERE rowid=" + userid + ";");
//        User retUser = null;
//        try {
//            retUser = new User(rs.getString("nickname"), userid);
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        }
//        return retUser;
//    }
}
