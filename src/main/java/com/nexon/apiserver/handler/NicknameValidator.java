package com.nexon.apiserver.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chan8 on 2017-02-06.
 */
public class NicknameValidator {
    public static final int ALPHA_NUMERIC = 0;
    public static final int LONGER_THAN_TWENTY = 1;
    public static final int SPECIAL_LETTER = 2;
    
    public NicknameValidator() {
    }
    
    public int isValidateName(String nickname) {
        Pattern notAlphaNumeric = Pattern.compile("\\W");
        Matcher matcher = notAlphaNumeric.matcher(nickname);
        
        if (matcher.find()) 
            return SPECIAL_LETTER;
        if (nickname.length() > 20)
            return LONGER_THAN_TWENTY;
        else
            return ALPHA_NUMERIC;
    }
}
