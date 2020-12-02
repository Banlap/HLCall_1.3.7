package hzhl.net.hlcall.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static String getCurrentTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 获取当前时间
     *
     * @param pattern
     * @return
     */
    public static String getCurDate(String pattern) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date());
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param milSecond
     * @param pattern
     * @return
     */
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static String getDateToString(long milSecond) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static Calendar dataToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String getMonthDay() {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");
        String result = format.format(new Date());
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return result;
    }

    public static String getWeekday() {
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(Calendar.DAY_OF_WEEK);
        String weekday;
        switch (i) {
            case 1:
                weekday = "星期天";
                break;
            case 2:
                weekday = "星期一";
                break;
            case 3:
                weekday = "星期二";
                break;
            case 4:
                weekday = "星期三";
                break;
            case 5:
                weekday = "星期四";
                break;
            case 6:
                weekday = "星期五";
                break;
            case 7:
                weekday = "星期六";
                break;
            default:
                weekday = "";
        }
        return weekday;
    }

    /*
    @param
    dt
     *@return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * 根据日期字符串判断当月第几周
     *
     * @param str
     * @return
     * @throws Exception
     */
    public static int getWeek(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //第几周
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        //第几天，从周日开始
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    /**
     * 根据日期字符串判断当月第几周
     *
     * @return
     * @throws Exception
     */
    public static int getWeek() {
        Calendar calendar = Calendar.getInstance();
        //  calendar.setTime(new Date());
        //第几周
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        //第几天，从周日开始
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    /**
     * 当月有几周
     */
    public static int getWeeksOfMonth(String s) {
        Date date = strToDate(s);
        if (date == null) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //第几周
        int week = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);

        return week;
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public static String getDateMMdd(String date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String mmdd = "";

        try {
            Date d = f.parse(date);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd");
            mmdd = format.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mmdd;
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd
     */
    public static String getCurrentDateYYMMdd() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd
     */
    public static String getDateYYMMdd(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String getDateHHmmm(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String getDateMMdd(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String getDateMMddMonthDay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String getDateMMddYearMonth(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月");
        String dateString = formatter.format(date);
        return dateString;
    }

    //根据秒数转化为时分秒   00:00:00
    public static String getTime(int second) {

        if (second < 60) {
            return second + "秒";
        }
        if (second < 3600) {
            int minute = second / 60;
            second = second - minute * 60;

            return minute + "分" + second + "秒";
        }
        int hour = second / 3600;
        int minute = (second - hour * 3600) / 60;
        second = second - hour * 3600 - minute * 60;

        return hour + "时" + minute + "分" + second + "秒";
    }

}
