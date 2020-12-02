package hzhl.net.hlcall.utils;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static final String URL_REG_EXPRESSION = "^(https?://)?([a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+)+(/*[A-Za-z0-9/\\-_&:?\\+=//.%]*)*";
    public static final String EMAIL_REG_EXPRESSION = "\\w+(\\.\\w+)*@\\w+(\\.\\w+)+";
    public static final String PATTERN_CUTMOBILENUM = "^1(3[0-2]|4[5]|5[56]|7[56]|8[56])\\d{8}$";

    /**
     * 是否手机号码
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[5678]|18[0-9]|14[57])[0-9]{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }


    /**
     * 验证号码 手机号 固话均可
     *
     * @param phoneNumber
     * @return
     */
    public static boolean isPhoneNumberValid(String phoneNumber) {
        boolean isValid = false;

        String expression = "((^(13|15|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|(^0[3-9] {1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|(^0[3-9]{1}\\d{2}-? \\d{7,8}-(\\d{1,4})$))";
        CharSequence inputStr = phoneNumber;

        Pattern pattern = Pattern.compile(expression);

        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }

        return isValid;

    }

    /**
     * @param @param  s
     * @param @return
     * @return int
     * @Title: checkPwd
     * @Description: 验证由数字, 大小写字母和特殊符号组成的密码
     */
    public static boolean isPwd(String s) {
        boolean flag = false;
        if (s.length() < 8) {
            return false;
        }
        String res = "[\u4e00-\u9fa5]";
        Pattern pattern0 = Pattern.compile(res);
        Matcher mat0 = pattern0.matcher(s);
        if (mat0.find()) {
            System.out.println("find");
            return false;
        }

        int ls = 0;
        String comp1 = "([a-z])+";
        Pattern pattern1 = Pattern.compile(comp1);
        Matcher mat1 = pattern1.matcher(s);
        if (mat1.find()) {
            ls++;
        }
        Pattern pattern2 = Pattern.compile("([0-9])+");
        Matcher mat2 = pattern2.matcher(s);
        if (mat2.find()) {
            ls++;
        }

        Pattern pattern3 = Pattern.compile("([A-Z])+");
        Matcher mat3 = pattern3.matcher(s);
        if (mat3.find()) {
            ls++;
        }
        Pattern pattern4 = Pattern.compile("[^a-zA-Z0-9]+");
        Matcher mat4 = pattern4.matcher(s);
        if (mat4.find()) {
            ls++;
        }
        if (ls == 4) {
            flag = true;
            return flag;
        }
        return flag;
    }

    public static boolean isAccount(String res) {
        boolean tag = false;
        final String comp = "[a-zA-Z][a-zA-Z0-9_]*";
        final Pattern pattern = Pattern.compile(comp);
        final Matcher mat = pattern.matcher(res);
        if (mat.matches()) {
            tag = true;
        }
        return tag;
    }

    /**
     * 判断是否是中国联通的号码
     *
     * @param mobile
     * @return boolean
     * @Title: isUnicomMobileNO
     * @Description: TODO
     */
    public static boolean isUnicomMobileNO(String mobile) {
        Pattern p = Pattern.compile(PATTERN_CUTMOBILENUM);
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    public static boolean isUrl(String s) {
        if (s == null) {
            return false;
        }
        return Pattern.matches(URL_REG_EXPRESSION, s);
    }

    public static boolean isUniconPhone(String phone) {
        boolean tag = false;
        final Pattern pattern = Pattern.compile(PATTERN_CUTMOBILENUM);
        final Matcher mat = pattern.matcher(phone);
        if (mat.matches()) {
            tag = true;
        }
        return tag;
    }

    public static boolean isEmail(String s) {
        if (s == null) {
            return true;
        }
        return Pattern.matches(EMAIL_REG_EXPRESSION, s);
    }

    public static boolean isBlank(String s) {
        if (s == null) {
            return true;
        }
        return Pattern.matches("\\s*", s);
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * @param param
     * @return boolean
     * @Title: isNull
     * @Description: 空对象判定
     */
    public static boolean isNull(Object param) {
        if (param == null)
            return true;
        else
            return false;
    }

    /**
     * 判断字符串是否是整数
     *
     * @param s
     * @return
     */
    public static boolean isInteger(String s) {
        if (s == null || "".equals(s)) {
            return false;
        }
        return s.matches("^\\d+$");
    }

    /**
     * @param @param  num
     * @param @return
     * @return boolean
     * @Title: isNum
     * @Description: 是否为数字
     */
    public static boolean isNum(String num) {
        if (num == null || "".equals(num)) {
            return false;
        }
        final String reg = "\\d+\\.{0,1}\\d*";

        return num.matches(reg);
    }

    /**
     * @param s
     * @return
    可以替换大部分空白字符， 不限于空格
    \s 可以匹配空格、制表符、换页符等空白字符的其中任意一个
     */
    public static String delBlankString(String s) {
        String str = "";
        if (!isEmpty(s)) {
            str = s.replaceAll("\\s*", "");
        }
        return str;
    }

    /**
     * @param @param  s 需要转换的
     * @param @return
     * @return int
     * @Title: parseToInt
     * @Description: 将字符串转换为整数
     */
    @SuppressWarnings("finally")
    public static int parseToInt(String s) {
        int value = 0;
        try {
            value = Integer.parseInt(s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: splitByCityState
     * @Description: 截取市、州的名称
     */
    public static String splitByCityState(String source) {
        String target = "";
        if (source == null || "".equals(source)) {
            return target;
        }
        if (source.contains("市")) {
            target = source.substring(0, source.indexOf("市"));
        } else if (source.contains("州")) {
            target = source.substring(0, source.indexOf("州"));
        } else {
            target = source;
        }
        return target;
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: toTenThround
     * @Description: 将数字转换为1万，并保留小数点后四位
     */
    public static String toTenThround(String source) {
        String target = "0.0";
        try {
            Double targetValue = Double.parseDouble(source);
            targetValue = targetValue / 10000;
            DecimalFormat format = new DecimalFormat("0.0000");
            target = format.format(targetValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return target;
    }


    public static String mToKm(String source) {
        String target = "0.0";
        try {
            Double targetValue = Double.parseDouble(source);
            if (targetValue < 1000) {
                return source + "m";
            }
            targetValue = targetValue / 1000;
            DecimalFormat format = new DecimalFormat("0.00");
            target = format.format(targetValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return target + "km";
    }

    public static String mToKmChina(String source) {
        String target = "0.0";
        try {
            Double targetValue = Double.parseDouble(source);
            if (targetValue < 1000) {
                return source + "米";
            }
            targetValue = targetValue / 1000;
            DecimalFormat format = new DecimalFormat("0.00");
            target = format.format(targetValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return target + "千米";
    }

    public static String formatBalance(String source) {
        String target = "0.00";
        try {
            Double targetValue = Double.parseDouble(source);
            DecimalFormat format = new DecimalFormat("0.00");
            target = format.format(targetValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return target;
    }

    public static String formatOneDouble(String source) {
        String target = "0.0";
        try {
            Double targetValue = Double.parseDouble(source);
            DecimalFormat format = new DecimalFormat("0.0");
            target = format.format(targetValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * @param @param  sourceValue
     * @param @return
     * @return double
     * @Title: parseDouble
     * @Description: 将字符串转换为小数
     */
    public static double parseDouble(String sourceValue) {
        double target = 0.0;
        try {
            target = Double.parseDouble(sourceValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * 结果带有，例如：1,111.23
     *
     * @param @param  source
     * @param @return
     * @return String
     * @Title: convertTwoDecimal
     * @Description: 保留两位小数
     */
    public static String convertTwoDecimal(double source) {
        String target = "0.00";
        try {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(2);
            target = nf.format(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: convertOneDecimal
     * @Description: 保留1位小数
     */
    public static String convertOneDecimal(double source) {
        String target = "0.0";
        try {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(1);
            target = nf.format(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: convertFourDecimal
     * @Description: 保留4位小数
     */
    public static String convertFourDecimal(double source) {
        String target = "0.0000";
        try {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(4);
            target = nf.format(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: splitBySpace
     * @Description: 根据空格拆分字符串并返回其第一节
     */
    public static String splitBySpace(String source) {
        String value = "";
        try {
            String[] args = source.split(" ");
            if (args != null && args.length > 0) {
                if (args[0] != null && !"".equals(args[0])) {
                    value = args[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * @param @param  c
     * @param @return
     * @return boolean
     * @Title: isChinese
     * @Description: 是否为中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * @param @param  task 任务
     * @param @param  finish 完成量
     * @param @return
     * @return String
     * @Title: getPercent
     * @Description: 获取百分比
     */
    public static String getPercent(String task, String finish) {
        String value = "0%";
        try {
            double taskValue = Double.parseDouble(task);
            double finishValue = Double.parseDouble(finish);
            if (taskValue > 0 && finishValue > 0) {
                double percentValue = finishValue / taskValue * 100;
                value = String.valueOf(Math.floor(percentValue)) + "%";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: getSixFraction
     * @Description: 保留小数点后6位
     */
    public static String getSixFraction(double source) {
        String target = "0.000000";
        try {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(6);
            target = nf.format(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * @param @param  date 生日
     * @param @param  formate 日期格式
     * @param @return
     * @return int[] 年龄：岁在前，月在后
     * @Title: getAgeByDate
     * @Description: 根据生日计算年龄，只精确到月，如2岁3个月
     */
    @SuppressLint("SimpleDateFormat")
    public static int[] getAgeByDate(String date, String formate) {
        int[] age = new int[2];
        if (isEmpty(date) || isEmpty(formate)) {
            return age;
        }
        try {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat(formate);

            Date birthDate = dateFormat.parse(date);
            calendar.setTime(birthDate);
            int birthYear = calendar.get(Calendar.YEAR);
            int birthMonth = calendar.get(Calendar.MONTH) + 1;
            int birthDay = calendar.get(Calendar.DAY_OF_MONTH);

            Date nowDate = new Date();
            calendar.setTime(nowDate);
            int nowYear = calendar.get(Calendar.YEAR);
            int nowMonth = calendar.get(Calendar.MONTH) + 1;
            int nowDay = calendar.get(Calendar.DAY_OF_MONTH);

            int year = 0;
            int month = 0;
            if (nowDate.compareTo(birthDate) < 0) {
                return age;
            } else {
                year = nowYear - birthYear;
                if (nowMonth > birthMonth) {
                    month = nowMonth - birthMonth;
                } else {
                    if (nowMonth == birthMonth) {
                        if (nowDay < birthDay) {
                            month = 11;
                            year--;
                        }
                    } else {
                        month = 12 - birthMonth + nowMonth;
                        year--;
                    }
                }
            }
            age[0] = year;
            age[1] = month;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return age;
    }

    /**
     * @param @param  idCard
     * @param @return
     * @return boolean
     * @Title: isValidatedIDCard
     * @Description: 是否为有效的身份证号码
     */
    public static boolean isValidatedIDCard(String idCard) {
        if (isEmpty(idCard)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(\\d{14}|\\d{17})(\\d|[xX])$");
        Matcher m = pattern.matcher(idCard);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param @param  str
     * @param @return
     * @return boolean
     * @Title: isValidatedPassWord
     * @Description: 判断是否含有大小写字母, 数字, 特殊符号的密码
     */
    public static boolean isValidatedPassWord(String str) {
        int num = 0;
        num = Pattern.compile("\\d").matcher(str).find() ? num + 1 : num;
        num = Pattern.compile("[a-z]").matcher(str).find() ? num + 1 : num;
        num = Pattern.compile("[A-Z]").matcher(str).find() ? num + 1 : num;
        num = Pattern.compile("[-.!@#$%^&*()+?><]").matcher(str).find() ? num + 1
                : num;
        return num >= 3;
    }

    /**
     * @param @param  source
     * @param @return
     * @return String
     * @Title: convertThreeFraction
     * @Description: 将字符串转换为保留3位小数点的数字
     */
    public static String convertThreeFraction(String source) {
        double target = 0;
        try {
            target = Double.parseDouble(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        return String.format("%.3f", target);
//    	BigDecimal bd = new BigDecimal(target);
//    	target = bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//    	return target;
    }

    /**
     * @param @param  source
     * @param @return
     * @return boolean
     * @Title: isSpaceMiddle
     * @Description: 字符串中间是否有空格符号
     */
    public static boolean isSpaceMiddle(CharSequence source) {
        boolean isSpaceMiddle = false;
        try {
            if (source.length() > 2) {
                for (int i = 1; i < source.length() - 1; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        isSpaceMiddle = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSpaceMiddle;
    }

    /**
     * @param @param  sourceValue
     * @param @return
     * @return float
     * @Title: parseFloat
     * @Description: TODO
     */
    public static float parseFloat(String sourceValue) {
        float target = 0.0f;
        try {
            target = Float.parseFloat(sourceValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

   /* public static String locationSubStr(LatLng latLonPoint) {
        double lat = latLonPoint.latitude;
        double lng = latLonPoint.longitude;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getSixFraction(lng));
        stringBuffer.append(",");
        stringBuffer.append(getSixFraction(lat));
        return stringBuffer.toString();
    }*/


    /**
     * 判断字符串是否为null或长度为0
     *
     * @param s 待校验字符串
     * @return {@code true}: 空<br> {@code false}: 不为空
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    /**
     * 判断字符串是否为null或全为空格
     *
     * @param s 待校验字符串
     * @return {@code true}: null或全空格<br> {@code false}: 不为null且不全空格
     */
    public static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

    /**
     * 判断两字符串是否相等
     *
     * @param a 待校验字符串a
     * @param b 待校验字符串b
     * @return {@code true}: 相等<br>{@code false}: 不相等
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

}
