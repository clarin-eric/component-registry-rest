/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.componentregistry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 *
 * @author olhsha
 */
public class DatesHelper {

    public final static Locale RFC822DATEFORMAT_LOCALE = Locale.of("en", "US", "POSIX");
    public final static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
            "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", RFC822DATEFORMAT_LOCALE);

    /**
     *
     * @param dateString the value of dateString
     */
    public static Date parseWorks(String dateString) {
        try {
            return ComponentUtils.getDate(dateString);
        } catch (ParseException pe) {
            return null;
        }
    }

    /**
     *
     * @param date1 the value of date1
     * @param date2 the value of date2
     * @return -1 if d1 is younger than d2, 1 if d1 is older then d2
     */
    public static int compareDateStrings(String date1, String date2) {
        final Date d1 = parseWorks(date1);
        final Date d2 = parseWorks(date2);
        if (d1 == null) {
            if (d2 == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (d2 == null) {
                return -1;
            } else {
                return d2.compareTo(d1);
            }
        }
    }

    /**
     *
     * @param dateString the value of dateString
     */
    public static String getRFCDateTime(String dateString) {
        final Date date = parseWorks(dateString);
        if (date == null) {
            return dateString;
        } else {
            return RFC822DATEFORMAT.format(date);
        }
    }

    /**
     *
     * @param date
     * @return String representation
     */
    public static String getRFCDateTime(Date date) {
        if (date == null) {
            return null;
        }
        String s = DateFormatUtils.format(date,
                DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern());
        return getRFCDateTime(s);
    }

    public static Date parseRFCDateTime(String dateTime) {
        try {
            return RFC822DATEFORMAT.parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String isoFormat(Date d) {
        return createNewDate(d.getTime());
    }

    public static String createNewDate() {
        return createNewDate(new Date().getTime());
    }

    public static Date parseIso(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern());
        try {
            return sdf.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String createNewDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern());
        return sdf.format(new Date(time));
    }

    public static String formatXmlDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZ");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String s = sdf.format(date);
        s = s.substring(0, s.length() - 2) + ":" + s.substring(s.length() - 2);
        return s;
    }

    public static Date parseXmlDateTime(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZ");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            //strip timezone colom
            date = date.replaceAll("(.*?T.*?[+-])(\\d\\d):(\\d\\d)", "$1$2$3");
            return sdf.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

}
