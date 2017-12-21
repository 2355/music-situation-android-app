package com.example.tlabuser.musicapplication;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by tlabuser on 2017/12/20.
 */

public class CalendarUtil {

    public static String calToStr(Calendar cal) {
        return (String) DateFormat.format("yyyy/MM/dd, E, kk:mm:ss", cal);
    }

    public static Calendar strToCal(String str) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd, E, kk:mm:ss");

        try {
            cal.setTime(sdf.parse(str));
        } catch (ParseException e) {
            cal = null;
        }

        return cal;
    }

    public static List<String> calToSituations(Calendar cal) {
        List<String> situations = new ArrayList<>();

        switch (cal.get(Calendar.MONTH)){
            case Calendar.JANUARY: situations.add("1月"); situations.add("冬"); break;
            case Calendar.FEBRUARY: situations.add("2月"); situations.add("冬"); break;
            case Calendar.MARCH: situations.add("3月"); situations.add("春"); break;
            case Calendar.APRIL: situations.add("4月"); situations.add("春"); break;
            case Calendar.MAY: situations.add("5月"); situations.add("春"); break;
            case Calendar.JUNE: situations.add("6月"); situations.add("夏"); break;
            case Calendar.JULY: situations.add("7月"); situations.add("夏"); break;
            case Calendar.AUGUST: situations.add("8月"); situations.add("夏"); break;
            case Calendar.SEPTEMBER: situations.add("9月"); situations.add("秋"); break;
            case Calendar.OCTOBER: situations.add("10月"); situations.add("秋"); break;
            case Calendar.NOVEMBER: situations.add("11月"); situations.add("秋"); break;
            case Calendar.DECEMBER: situations.add("12月"); situations.add("冬"); break;
        }

        switch (cal.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY: situations.add("日曜日"); break;
            case Calendar.MONDAY: situations.add("月曜日"); break;
            case Calendar.TUESDAY: situations.add("火曜日"); break;
            case Calendar.WEDNESDAY: situations.add("水曜日"); break;
            case Calendar.THURSDAY: situations.add("木曜日"); break;
            case Calendar.FRIDAY: situations.add("金曜日"); break;
            case Calendar.SATURDAY: situations.add("土曜日"); break;
        }

        //if (hour)

        switch (cal.get(Calendar.HOUR_OF_DAY)){
            case 0:
            case 1:
            case 2: situations.add("深夜"); break;
            case 3:
            case 4:
            case 5: situations.add("明け方"); break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10: situations.add("朝"); break;
            case 11:
            case 12:
            case 13:
            case 14: situations.add("昼"); break;
            case 15:
            case 16:
            case 17: situations.add("夕方"); break;
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23: situations.add("夜"); break;
        }

        return situations;
    }
}
