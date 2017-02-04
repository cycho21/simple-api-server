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
        dao.createTable();
//        System.out.println("SETUP OK");
    }

    @Test
    public void testInsertOperation() {
//        System.out.println("TEST INSERT START");
        int id = dao.addUser("chanyeon").getUserid();
//        System.out.println(dao.getUser(id).getNickname());
    }

    @After
    public void coolDown() {
        dao.dropTable();
    }
}