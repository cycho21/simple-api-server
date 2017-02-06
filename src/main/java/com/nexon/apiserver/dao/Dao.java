package com.nexon.apiserver.dao;

/**
 * Created by Administrator on 2017-02-04.
 */
public class Dao {

    private final String fileName;
    private final SimpleSqliteTemplate jdbcTemplate;

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
        User user = jdbcTemplate.executeQuery("SELECT rowid FROM users WHERE nickname='" + nickname + "';");
        user.setNickname(nickname);
        return user;
    }

    private User getUser(String nickname) {
        User user = jdbcTemplate.executeQuery("SELECT rowid FROM users WHERE nickname='" + nickname + "';");
        user.setNickname(nickname);
        return user;
    }

    public User getUser(int userid) {
        User user = jdbcTemplate.executeQueryByUserId("SELECT nickname FROM users WHERE rowid=" + userid + ";");
        if (user.getNickname() != null)
            user.setUserid(userid);
        return user;
    }
    
    public User updateUser(int userid, String nickname) {
        if (getUser(userid).getUserid() != 0) {
            jdbcTemplate.executeUpdate("UPDATE users SET nickname='" + nickname + "' WHERE rowid=" + userid + ";");
        } else {
            return null;
        }
        User user = getUser(nickname);
        return user;
    }
    
    public void deleteUser(int userid) {
        jdbcTemplate.executeUpdate("DELETE FROM users WHERE rowid=" + userid + ";");
    }
}
