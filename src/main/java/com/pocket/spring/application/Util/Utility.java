package com.pocket.spring.application.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {



    public static boolean isValidEmail(String email) {

        final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
