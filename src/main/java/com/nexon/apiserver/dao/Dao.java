package com.nexon.apiserver.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        dropTable();
        createTable();
    }

    public void dropTable() {
        jdbcTemplate.executeUpdate("DROP TABLE users");
    }

    public void createTable() {
        jdbcTemplate.executeUpdate("CREATE TABLE users (nickname VARACHAR(20) not NULL);");
    }

    public User addUser(String nickname) {
        if (getUser(nickname).getUserid() != 0) {
            System.out.println("Nickname exists!");
            return null;
        }

        jdbcTemplate.executeUpdate("INSERT INTO users values ('" + nickname + "');");
        int userid = 0;
        User user = jdbcTemplate.executeQuery("SELECT rowid FROM users WHERE nickname='" + nickname + "';");
        user.setNickname(nickname);
        return user;
    }

    private User getUser(String nickname) {
        int userid = 0;
        User user = jdbcTemplate.executeQuery("SELECT rowid FROM users WHERE nickname='" + nickname + "';");
        user.setNickname(nickname);
        return user;
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
