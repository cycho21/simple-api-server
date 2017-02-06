package com.nexon.apiserver.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Dao {
    private final SimpleSqliteTemplate jdbcTemplate;

    public Dao() {
        this.jdbcTemplate = new SimpleSqliteTemplate();
    }

    public void dropUsersTable() {
        jdbcTemplate.executeUpdate(jdbcTemplate.preparedStatement("DROP TABLE users"));
    }

    public void createUsersTable() {
        jdbcTemplate.executeUpdate(jdbcTemplate.preparedStatement("CREATE TABLE users (userid INTEGER PRIMARY KEY," +
                "nickname VARACHAR(20) not NULL);"));
    }

    public User addUser(String nickname) {
        if (getUser(nickname).getUserid() != 0) {
            System.out.println("Nickname exists!");
            return null;
        }

        PreparedStatement preparedStatement = jdbcTemplate.preparedStatement("INSERT INTO users (nickname) values (?);");
        try {
            preparedStatement.setString(1, nickname);
            jdbcTemplate.executeUpdate(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        preparedStatement = jdbcTemplate.preparedStatement("SELECT userid FROM users WHERE nickname=?;");
        try {
            preparedStatement.setString(1, nickname);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        User user = (User) jdbcTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.USER);
        user.setNickname(nickname);
        return user;
    }

    private User getUser(String nickname) {
        PreparedStatement preparedStatement = jdbcTemplate.preparedStatement("SELECT userid FROM users WHERE nickname=?;");
        try {
            preparedStatement.setString(1, nickname);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        User user = (User) jdbcTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.USER);
        user.setNickname(nickname);
        return user;
    }

    public User getUser(int userid) {
        PreparedStatement preparedStatement = jdbcTemplate.preparedStatement("SELECT nickname FROM users WHERE userid=?;");
        try {
            preparedStatement.setInt(1, userid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        User user = (User) jdbcTemplate.executeQuery(preparedStatement, SimpleSqliteTemplate.USER);
        if (user.getNickname() != null)
            user.setUserid(userid);
        return user;
    }

    public User updateUser(int userid, String nickname) {
        if (getUser(userid).getUserid() != 0) {
            PreparedStatement preparedStatement = jdbcTemplate.preparedStatement("UPDATE users SET nickname=? WHERE userid=?;");
            try {
                preparedStatement.setString(1, nickname);
                preparedStatement.setInt(2, userid);
                jdbcTemplate.executeUpdate(preparedStatement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        
        User user = getUser(nickname);
        return user;
    }

    public void deleteUser(int userid) {
        PreparedStatement preparedStatement = jdbcTemplate.preparedStatement("DELETE FROM users WHERE userid=?;");
        try {
            preparedStatement.setInt(1, userid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        jdbcTemplate.executeUpdate(preparedStatement);
    }

}
