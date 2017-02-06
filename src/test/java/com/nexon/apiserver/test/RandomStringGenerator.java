package com.nexon.apiserver.test;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by chan8 on 2017-02-06.
 */
public class RandomStringGenerator {

    private char[] symbols;
    private Random random;

    public RandomStringGenerator() {
    }
    
    public void initialize() {
        this.random = new Random();
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }
    
    public String nextRandomString(int length) {
        char[] buffer = new char[length];
        for (int idx = 0; idx < length; ++idx) {
            buffer[idx] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buffer);
    }
}
