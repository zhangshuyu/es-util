package com.hansight.es.utils;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期工具类
 *
 * @author decai
 */
public class DateTools extends DateUtils {
    private static Logger LOG = LoggerFactory.getLogger(DateTools.class);
    private static final String DATEPATTERN = "yyyy-MM-dd";
    private static final String TIMEPATTERN = "HH:mm:ss";
    private static final String DATETIMEPATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将字符串转为Date类型,支持"yyyy-MM-dd","HH:mm:ss","yyyy-MM-dd HH:mm:ss"
     *
     * @param dateString
     * @return
     */
    public static Date parseToDate(String dateString) {
        try {
            return DateUtils.parseDate(dateString, new String[]{DATEPATTERN, DATETIMEPATTERN, TIMEPATTERN});
        } catch (ParseException e) {
            LOG.debug("{}无法转换为日期", dateString, e);
            return null;
        }
    }

    /**
     * 将日期转为字符串 "yyyy-MM-dd"
     *
     * @param date
     * @return
     */
    public static String formatToDateString(Date date) {
        return FastDateFormat.getInstance(DATEPATTERN).format(date);
    }

    public static String formatToDateString(Date date, String pattern) {
        return FastDateFormat.getInstance(pattern).format(date);
    }

    /**
     * 将日期转为字符串 "yyyy-MM-dd HH:mm:ss"
     *
     * @param date
     * @return
     */
    public static String formatToDateTime(Date date) {
        return FastDateFormat.getInstance(DATETIMEPATTERN).format(date);
    }

    /**
     * 将日期转为字符串 "HH:mm:ss"
     *
     * @param date
     * @return
     */
    public static String formatToTimeString(Date date) {
        return FastDateFormat.getInstance(TIMEPATTERN).format(date);
    }

    /**
     * 将毫秒转为字符串 "yyyy-MM-dd HH:mm:ss"
     *
     * @param msec
     * @return
     */
    public static String formatMsecToTimeString(long msec) {
        return FastDateFormat.getInstance(DATETIMEPATTERN).format(msec);
    }

    public static Date addYear(Date date) {
        long current = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(current);
        int year = c.get(Calendar.YEAR);
        c.setTime(date);
        c.set(Calendar.YEAR, year);
        Date d = c.getTime();
        if (d.getTime() <= current) {
            return d;
        }
        c.add(Calendar.YEAR, -1);
        return c.getTime();
    }

    /**
     * Get UTC format date time
     *
     * @param datePattern
     * @param formattedDateTime
     * @return utc format datetime
     */
    public static String getUTCDateTime(String datePattern, String formattedDateTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(datePattern);
        DateTime dt = formatter.parseDateTime(formattedDateTime);
        return dt.toDateTime(DateTimeZone.UTC).toString();
    }

    /**
     * Get UTC format date time
     *
     * @param time
     * @return
     */
    public static String getUTCDateTime(long time) {
        DateTime dt = new DateTime(time);
        return dt.toDateTime(DateTimeZone.UTC).toString();
    }

    /**
     * Get timestamp
     *
     * @param utcDateTime
     * @return
     */
    public static long getTimestamp(String utcDateTime) {
        DateTime dt = new DateTime(utcDateTime);
        return dt.getMillis();
    }

    /**
     * Get timestamp
     *
     * @param utcDateTime
     * @return
     */
    public static Date getDate(String utcDateTime) {
        DateTime dt = new DateTime(utcDateTime);
        return dt.toDate();
    }

    public static long getDayBegin(long date) {
        return getDayBegin(date, DateTimeZone.UTC);
    }

    public static long getDayBegin(long date, long hoursOffsetFromZeroZone) {
        return getDayBegin(date, DateTimeZone.forOffsetHours(8));
    }

    public static long getDayBegin(long date, DateTimeZone zone) {
        DateTime dt = new DateTime(date, zone);
        return dt.minusHours(dt.getHourOfDay()).minusMinutes(dt.getMinuteOfHour()).minusSeconds(dt.getSecondOfMinute()).minusMillis(dt.getMillisOfSecond()).getMillis();
    }

    /***
     * 取得起止日期间的天数集合
     *
     * @param startDate
     * @param endDate
     * @return List<String>
     */
    public static List<String> getDaysBetweenStartAndEndDate(String startDate, String endDate, SimpleDateFormat sdf) {
        List<String> al = new ArrayList<String>();
        if (startDate.equals(endDate)) {
            // IF起始日期等于截止日期,仅返回起始日期一天
            al.add(startDate);
        } else if (startDate.compareTo(endDate) < 0) {
            // IF起始日期早于截止日期,返回起止日期的每一天
            while (startDate.compareTo(endDate) < 0) {
                al.add(startDate);
                try {
                    Long l = sdf.parse(startDate).getTime();
                    startDate = sdf.format(l + 3600 * 24 * 1000);// +1天
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // IF起始日期晚于截止日期,仅返回起始日期一天
            al.add(startDate);
        }
        return al;
    }

    /**
     * 分割时间区间
     * @param start 开始时间
     * @param end 结束时间
     * @param interval 时间粒度（单位：毫秒 例如： 5 minute = 5 * 60 * 1000）
     * @return 返回分割好的时间列表
     */
    public static List<Date> splitDateInterval(Date start, Date end, long interval) {
        List<Date> dates = new ArrayList<>();
        if (start.getTime() < end.getTime()) {
            long temp = start.getTime();
            while (temp <= end.getTime()) {
                dates.add(new Date(temp));
                temp += interval;
            }
            if ((temp - interval) < end.getTime()) dates.add(end);
        }
        return dates;
    }
    

    /**
     * get timestamp 
     * @param datePattern
     * @param formattedDateTime
     * @return
     */
    public static long getTimestamp(String datePattern, String formattedDateTime){
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        Date date;
        long timestamp = 0L;
        try {
            date = sdf.parse(formattedDateTime);
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            timestamp = now.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }
}
