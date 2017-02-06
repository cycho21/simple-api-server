package com.nexon.apiserver.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Administrator on 2017-02-04.
 */
public class DaoTest {

    private Dao dao;

    @Before
    public void setUp() {
        this.dao = new Dao();
        dao.dropUsersTable();
        dao.createUsersTable();
        System.out.println("SETUP OK");
    }

    @Test
    public void testInsertOperation() {
        System.out.println("TEST INSERT START");
        int id = dao.addUser("chanyeon").getUserid();
        dao.addUser("c2");
        dao.addUser("c3");
        dao.addUser("c4");
        dao.addUser("c5");
        dao.addUser("c6");
        dao.updateUser(id, "chanyeon2");
        dao.deleteUser(1);
    }

//    @After
//    public void dropTable() {
//        dao.dropUsersTable();
//    }
}