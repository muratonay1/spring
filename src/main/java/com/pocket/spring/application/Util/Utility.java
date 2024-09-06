package com.pocket.spring.application.Util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    public static String getRealDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate currentDate = LocalDate.now();
        return currentDate.format(formatter);
    }
    public static String getRealTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        LocalTime currentTime = LocalTime.now();
        return currentTime.format(formatter);
    }
    public static long getTimestamp(){
        return Instant.now().getEpochSecond();
    }

    public static String getDate(Date date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String cancelDate = dateFormatter.format(date);
        return cancelDate;
    }

    public static String getTime(Date date){
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmss");
        String cancelTime = timeFormatter.format(date);
        return cancelTime;
    }
    public static boolean isValidEmail(String email) {

        final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
