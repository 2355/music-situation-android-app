package com.example.tlabuser.musicapplication;

import com.example.tlabuser.musicapplication.Model.Situation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tlabuser on 2017/12/19.
 */

public class DateUtil {

    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static Date strToDate(String str) {
        Date date;

        try{
            date = format.parse(str);
            return date;
        }catch (ParseException e){
            return null;
        }
    }

    public static String dateToStr(Date date) {
        String str = format.format(date);

        return str;
    }

    public static List<Situation> dateToSituations(Date date) {
        List<Situation> situations = new ArrayList<>();

        return situations;
    }
}
