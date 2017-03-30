package fr.vcy.coredaemon.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XSDateTimeCustomBinder {
    
    public static final String DATE_HEURE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static Date parseDateTime(String s) {
        DateFormat formatter = new SimpleDateFormat(DATE_HEURE_FORMAT);
        try {
            return formatter.parse(s);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static String printDateTime(Date dt) {
        DateFormat formatter = new SimpleDateFormat(DATE_HEURE_FORMAT);
        return formatter.format(dt);
    }
    
    public static Date parseDate(String s) {
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        try {
            return formatter.parse(s);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static String printDate(Date dt) {
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(dt);
    }
    
}
