package hzhl.net.hlcall.utils;

/**
 * Created by elileo on 18/5/30.
 */

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final long DAY_OF_YEAR = 365;
    public static final long DAY_OF_MONTH = 30;
    public static final long HOUR_OF_DAY = 24;
    public static final long MIN_OF_HOUR = 60;
    public static final long SEC_OF_MIN = 60;
    public static final long MILLIS_OF_SEC = 1000;

    public static class YEARS {
        public static long toMillis(int years) {
            return years * DAY_OF_YEAR * HOUR_OF_DAY * MIN_OF_HOUR
                    * SEC_OF_MIN * MILLIS_OF_SEC;
        }

        public static long toSec(int years) {
            return years * DAY_OF_YEAR * HOUR_OF_DAY * MIN_OF_HOUR * SEC_OF_MIN;
        }

        public static long toMinute(int years) {
            return (years * DAY_OF_YEAR * HOUR_OF_DAY * MIN_OF_HOUR);
        }
    }

    public static class MONTHS {
        public static long toMillis(int months) {
            return months * DAY_OF_MONTH * HOUR_OF_DAY * MIN_OF_HOUR
                    * SEC_OF_MIN * MILLIS_OF_SEC;
        }

        public static long toSec(int months) {
            return months * DAY_OF_MONTH * HOUR_OF_DAY * MIN_OF_HOUR * SEC_OF_MIN;
        }

        public static long toMinute(int months) {
            return (months * DAY_OF_MONTH * HOUR_OF_DAY * MIN_OF_HOUR);
        }
    }

    public static class DAYS {
        public static long toMillis(int days) {
            return days * HOUR_OF_DAY * MIN_OF_HOUR * SEC_OF_MIN
                    * MILLIS_OF_SEC;
        }

        public static long toSec(int days) {
            return days * HOUR_OF_DAY * MIN_OF_HOUR * SEC_OF_MIN;
        }

        public static long toMinute(int days) {
            return (days * HOUR_OF_DAY * MIN_OF_HOUR);
        }
    }

    public static class HOURS {
        public static long toMillis(int hours) {
            return hours * MIN_OF_HOUR * SEC_OF_MIN * MILLIS_OF_SEC;
        }

        public static long toSec(int hours) {
            return hours * MIN_OF_HOUR * SEC_OF_MIN;
        }
    }

    public static class MINUTES {
        public static long toMillis(int minutes) {
            return minutes * SEC_OF_MIN * MILLIS_OF_SEC;
        }

        public static long toSec(int minutes) {
            return minutes * SEC_OF_MIN;
        }
    }

    public static class SECONDS {
        public static long toMillis(long seconds) {
            return seconds * MILLIS_OF_SEC;
        }

        public static int toSec(int seconds) {
            return seconds;
        }
    }

    public static class MILLIS {
        public static long toMillis(long millis) {
            return millis;
        }

        /**
         * Convert from millis to sec in int. This is valid when millis in UTC
         * is before year 2037.
         *
         * @param millis
         * @return Time in sec.
         */
        public static int toSec(long millis) {
            return (int) (millis / MILLIS_OF_SEC);
        }

        public static int toMinute(long millis) {
            return (int) (millis / MILLIS_OF_SEC / SEC_OF_MIN);
        }
    }

    public static long curTimeInMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Get current time in secs.
     *
     * @return Current time in seconds.
     */
    public static int curSec() {
        long l = System.currentTimeMillis();
        return (int) (l / MILLIS_OF_SEC);
    }

    /**
     * Get expired dead time, in secs.
     *
     * @param secsToExpire
     *            Seconds from now to the dead time.
     * @return Expired dead time, in secs, it is in UTC(GMT).
     */
    public static long getExpireDeadTime(long secsToExpire) {
        return (long) TimeUtils.curSec() + secsToExpire;
    }

    /**
     *
     * @param timestamp
     *            In millis.
     * @return
     */
    public static String getDisplayTimeFromTimestamp(long timestamp) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        return String.format("%d-%d-%d  (%02d:%02d)", year, month, day, hour,
                min);
    }

    /**
     * Return a string to represent time according to fmt
     *
     * @param fmt
     *            , must contains 5 years to hold, year, month, day, hour and
     *            min;
     * @param timestamp
     *            , time in millis
     * @return formatted string.
     */
    public static String timestampToString(String fmt, long timestamp) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        return String.format(fmt, year, month, day, hour, min);
    }

    public static boolean isSameWeek(long t1, long t2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(t1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(t2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR);
    }

    public static boolean isSameDay(long t1, long t2) {
        Date myDate1 = new Date(t1);
        Date myDate2 = new Date(t2);
        if (myDate1.getYear() != myDate2.getYear())
            return false;

        if (myDate1.getMonth() != myDate2.getMonth())
            return false;

        return myDate1.getDay() == myDate2.getDay();

    }

    public static boolean isSameMonth(long t1, long t2){
        Date myDate1 = new Date(t1);
        Date myDate2 = new Date(t2);
        if (myDate1.getYear() != myDate2.getYear())
            return false;
        return myDate1.getMonth() == myDate2.getMonth();
    }

    public static int getDayOfWeek(long t) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    private static String sToday = null;
    private static String sYesterday = null;
    private static String sBeforeYesterday = null;
    private static String sTimeFormat = null;
    private static String sShortDateFormat = null;
    private static String sDateFormat = null;
    private static String sSpace = " ";
    private static Calendar sCurrent = Calendar.getInstance();
    private static Calendar sPost = Calendar.getInstance();

    public static String getPostTimeString(long timeMillis, boolean showToday) {
        sCurrent.setTimeInMillis(System.currentTimeMillis());
        sPost.setTimeInMillis(timeMillis);
        int diffDays = sCurrent.get(Calendar.DAY_OF_YEAR)
                - sPost.get(Calendar.DAY_OF_YEAR);
        boolean isSameYear = sPost.get(Calendar.YEAR) == sCurrent
                .get(Calendar.YEAR);

        StringBuilder builder = new StringBuilder();
        if (diffDays > 0 || !isSameYear) {
            if (diffDays > 0 && diffDays <= 2) {
                builder.append(diffDays == 1 ? sYesterday : sBeforeYesterday);
            } else {
                if (isSameYear) {
                    builder.append(String.format(sShortDateFormat,
                            sPost.get(Calendar.MONTH) + 1,
                            sPost.get(Calendar.DAY_OF_MONTH)));
                } else {
                    builder.append(String.format(sDateFormat,
                            sPost.get(Calendar.YEAR),
                            sPost.get(Calendar.MONTH) + 1,
                            sPost.get(Calendar.DAY_OF_MONTH)));
                }
            }
        } else if (showToday) {
            builder.append(sToday);
        }
        builder.append(sSpace);
        builder.append(String.format(sTimeFormat,
                sPost.get(Calendar.HOUR_OF_DAY), sPost.get(Calendar.MINUTE)));
        return builder.toString();
    }


    public static String readableTime(int secs) {
        if (secs <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int minutes = secs / 60;
        int seconds = secs % 60;
        if (minutes < 10) {
            sb.append(0);
        }
        sb.append(minutes);
        sb.append(":");
        if (seconds < 10) {
            sb.append(0);
        }
        sb.append(seconds);
        return sb.toString();
    }
}

