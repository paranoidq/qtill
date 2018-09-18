package me.qtill.commons.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Date时间处理工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class DateUtil {

    public enum Format {
        ALL("yyyyMMddHHmmssSSS"),
        YMD_HMS("yyyyMMdd HH:mm:ss"),
        YMDHMS("yyyyMMddHHmmss"),
        Y_M_D("yyyy-MM-dd"),
        YMD("yyyyMMdd"),
        YM("yyyyMM"),
        HMS("HHmmss"),
        MD("MMdd"),
        HM("HH:mm");

        private String              formatString;
        private Map<String, Format> map;

        {
            Format[] formats = Format.values();
            for (Format f : formats) {
                map.put(f.formatString, f);
            }
        }

        Format(String formatString) {
            this.formatString = formatString;
        }

        public Format of(String formatString) {
            if (!map.containsKey(formatString)) {
                throw new IllegalArgumentException("invalid format string: [" + formatString + "]");
            }
            return map.get(formatString);
        }
    }

    private static ThreadLocal<Map<Format, SimpleDateFormat>> dateFormatThreaLocalMap = new ThreadLocal<>();

    static {
        Format[] formats = Format.values();
        for (Format format : formats) {
            dateFormatThreaLocalMap.get().put(format, new SimpleDateFormat(format.formatString));
        }
    }

    /**
     * 将Date转换为指定format的string
     *
     * @param date
     * @param format
     * @return
     */
    public static String getDate(Date date, Format format) {
        return dateFormatThreaLocalMap.get().get(format).format(date);
    }


    /**
     * 将Date转换为yyyyMMdd格式的string
     *
     * @param date
     * @return
     */
    public static String getDate(Date date) {
        return getDate(date, Format.YMD);
    }

    /**
     * 将Date转换为yyyyMMddHHmmss格式的string
     *
     * @param date
     * @return
     */
    public static String getDateTime(Date date) {
        return getDate(date, Format.YMDHMS);
    }


    /**
     * 将Date转换为HHmmss格式的string
     *
     * @param date
     * @return
     */
    public static String getHourMinuteSecond(Date date) {
        return getDate(date, Format.HMS);
    }

    /**
     * 将Date转换为MMdd格式的string
     *
     * @param date
     * @return
     */
    public static String getMonthDay(Date date) {
        return (new SimpleDateFormat("MMdd")).format(date);
    }

    /**
     * 增加日
     *
     * @param date
     * @param dayInterval
     * @return
     */
    public static Date addDay(Date date, int dayInterval) {
        return addDate(date, 5, dayInterval);
    }

    /**
     * 增加月
     *
     * @param date
     * @param monthInterval
     * @return
     */
    public static Date addMonth(Date date, int monthInterval) {
        return addDate(date, 2, monthInterval);
    }


    /**
     * 增加年
     *
     * @param date
     * @param yearInterval
     * @return
     */
    public static Date addYear(Date date, int yearInterval) {
        return addDate(date, 1, yearInterval);
    }

    /**
     * 增加日期
     *
     * @param date
     * @param field
     * @param interval
     * @return
     */
    public static Date addDate(Date date, int field, int interval) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(field, interval);
        return c.getTime();
    }

    /**
     * 解析String为Date对象
     *
     * @param time
     * @param format
     * @return
     */
    public static Date parseDate(String time, Format format) {
        try {
            SimpleDateFormat s = dateFormatThreaLocalMap.get().get(format);
            s.setLenient(false);
            return s.parse(time);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid time string, cannot parse to Date object", e);
        }
    }


    /**
     * 判断String是否符合format格式
     *
     * @param date
     * @param format
     * @return
     */
    public static boolean matchFormat(String time, Format format) {
        SimpleDateFormat s = dateFormatThreaLocalMap.get().get(format);
        try {
            s.setLenient(false);
            s.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
