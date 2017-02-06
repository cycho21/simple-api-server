package com.nexon.apiserver.handler;

import com.nexon.apiserver.dao.Dao;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by chanyeon on 2017-02-05.
 */
public class UserHandlerTest {

    private UserHandler userHandler;

    @Before
    public void setUp() {
        this.userHandler = new UserHandler(new Dao());
    }

    @Test
    public void testHandler() {

    }
}