package com.yiji.falcon.agent.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期工具类
 * @author QianLong
 * @createDate 2014-3-8
 */
public class DateUtil {

    /**
     * 将传入格式 格式化为指定格式
     *
     * @param date 传入格式
     * @return 转为指定格式 parrten
     */
    public static String parseToDateParrten(String date,String fromParrten,String toparrten) {
        if (StringUtils.isEmpty(date)) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(fromParrten);
        SimpleDateFormat fo = new SimpleDateFormat(toparrten);
        try {
            Date date1 = format.parse(date);
            return fo.format(date1);
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * 返回指定的时间格式
     *
     * @return
     */
    public static String getCurrentDate(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }
    /**
     * 获取传入指定天数前的日期
     * yyyy-MM-dd
     * @param day
     * 天数
     * @return
     */
    public static String getLastDateByCount(int day){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-day);
        calendar.add(Calendar.MONTH,1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        String monthS = month+"";
        String dateS = date + "";
        if(month <=9){
            monthS = "0" + month;
        }
        if(date <= 9){
            dateS = "0" + date;
        }
        return year+"-" + monthS + "-" + dateS;
    }

	/**
	 * 获取距当前日期上一个月的日期
	 * 返回格式：yyyy-MM-dd
	 * @return
	 */
	public static String getLastMonthDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);    //得到前一天
		calendar.add(Calendar.MONTH, -1);    //得到前一个月
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int date = calendar.get(Calendar.DATE);
		String monthS = month+"";
		String dateS = date + "";
		if(month <=9){
			monthS = "0" + month;
		}
		if(date <= 9){
			dateS = "0" + date;
		}
		return year+"-" + monthS + "-" + dateS;
	}
	
	/**
	 * 计算日期的天数
	 * @param startDate
     * yyyy-MM-dd
     * 开始日期
	 * @param endDate
     * yyyy-MM-dd
     * 结束日期
	 * @return
     * 两个日期相差的天数
	 */
	public static int getDateCount(String startDate,String endDate){
		Date sd = getDate(startDate);
		Date ed = getDate(endDate);
		long time = ed.getTime() - sd.getTime();
        return (int) (time/1000/60/60/24);
	}

    /**
     * 计算当前时间与传入的时间差（毫秒）
     * @param time
     * HH:mm:ss
     * @return
     * 返回0 代表出现异常错误，如传入的格式不对
     */
    public static long getTimeSubFromNowTime(String time){
        int year = Integer.parseInt(getLocationCurrentDate().split("-")[0]);
        int month = Integer.parseInt(getLocationCurrentDate().split("-")[1]);
        int day = Integer.parseInt(getLocationCurrentDate().split("-")[2]);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(year + "-" + month + "-" + day + " " + time);
            Date now = new Date();
            return date.getTime() - now.getTime();
        } catch (ParseException e) {
            return 0;
        }

    }

	/**
	 * 将指定的日期转化为Date对象
	 * @param date
	 * @return
     * 格式不对或匹配失败返回null
	 */
	@SuppressWarnings("deprecation")
	public static Date getParrtenDate(String date,String parrten){
        SimpleDateFormat dateFormat=new SimpleDateFormat(parrten);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 日期比较，当baseDate小于compareDate时，返回TRUE，当大于等于时，返回false
     *
     * @param format
     *            日期字符串格式
     * @param baseDate
     *            被比较的日期
     * @param compareDate
     *            比较日期
     * @return
     */
    public static boolean before(String format, String baseDate,
                                 String compareDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            Date base = simpleDateFormat.parse(baseDate);
            Date compare = simpleDateFormat.parse(compareDate);

            return compare.before(base);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * 日期比较，当baseDate大于compareDate时，返回TRUE，当小于等于时，返回false
     * @param format
     *            日期字符串格式
     * @param baseDate
     *            被比较的日期
     * @param compareDate
     *            比较日期
     * @return
     */
    public static boolean after(String format, String baseDate,
                                 String compareDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            Date base = simpleDateFormat.parse(baseDate);
            Date compare = simpleDateFormat.parse(compareDate);

            return compare.after(base);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
	 * 将‘yyyy-MM-dd’的字符串日期转化为Date对象
	 * @param date
	 * @return
     * 格式不对或匹配失败返回null
	 */
	@SuppressWarnings("deprecation")
	public static Date getDate(String date){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将Date转化为'yyyy-MM-dd HH:mm:ss'格式
     * @param date
     * @return
     */
    public static String getFormatDateTime(Date date){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

	/**
	 * 返回当前日期的yyyy-MM-dd的日期字符串格式
	 * @return
	 */
	public static String getLocationCurrentDate(){
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	/**
	 * 返回当前日期的HH:mm:ss的时间字符串格式
	 * @return
	 */
	public static String getLocationCurrentTime(){
		return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}

    /**
     * 获取传入日期与时间的Calendar对象
     * @param date
     * @param time
     * @return
     */
    @SuppressWarnings("deprecation")
    public static GregorianCalendar getDatetime(String date, String time){
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);
        int HH = Integer.parseInt(time.split(":")[0]);
        int MM = Integer.parseInt(time.split(":")[1]);
        int ss = Integer.parseInt(time.split(":")[2]);
        return new GregorianCalendar(year,month-1,day,HH,MM,ss);
    }

    /**
     * 计算传入的同一天的两个时间的时间差(毫秒)
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getTimeSubFromTime(String startTime,String endTime){
        int year = Integer.parseInt(getLocationCurrentDate().split("-")[0]);
        int month = Integer.parseInt(getLocationCurrentDate().split("-")[1]);
        int day = Integer.parseInt(getLocationCurrentDate().split("-")[2]);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startDate = format.parse(year + "-" + month + "-" + day + " " + startTime);
            Date endDate = format.parse(year + "-" + month + "-" + day + " " + endTime);
            return endDate.getTime() - startDate.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * 判断传入的时间是否在第二天2：30分之前
     * @param date
     * @param time
     * @return
     */
    public static boolean isDateForRecordDate(String date,String time){
        Date nowDate = getDatetime(date,time).getTime();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE,30);
        c.set(Calendar.SECOND,0);
        Date recordDate =c.getTime();
        return nowDate.getTime() <= recordDate.getTime();
    }

    /**
     * 判断传入的日期 + 1天后与当前系统时间相差几天
     * @param date
     * @return
     */
    public static int getDateSubFromNowDate(String date){
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);
        Calendar calendar = new GregorianCalendar(year,month,day);
        calendar.add(Calendar.MONTH,-1);
        calendar.add(Calendar.DAY_OF_MONTH,1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = formatter.format(calendar.getTime());
        Date fromDate;
        try {
            fromDate = formatter.parse(fromDateStr);
        } catch (ParseException e) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        String nowDateStr = formatter.format(cal.getTime());
        Date nowDate;
        try {
            nowDate = formatter.parse(nowDateStr);
        } catch (ParseException e) {
            return 0;
        }
        long time = nowDate.getTime() - fromDate.getTime();
        return (int)(time/1000/60/60/24);
    }

    /**
     * 获取当前日期与传入的日期相差几天
     * @param date
     * @return
     */
    public static int getDateSubNowDate(String date){
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);
        Calendar calendar = new GregorianCalendar(year,month,day);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = formatter.format(calendar.getTime());
        Date fromDate;
        try {
            fromDate = formatter.parse(fromDateStr);
        } catch (ParseException e) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        String nowDateStr = formatter.format(cal.getTime());
        Date nowDate;
        try {
            nowDate = formatter.parse(nowDateStr);
        } catch (ParseException e) {
            return 0;
        }
        long time = nowDate.getTime() - fromDate.getTime();
        return (int)(time/1000/60/60/24);
    }

    /**
     * 获取传入的日期所在年份共有多少天
     * @param date
     * @return
     */
    public static int getYearNumberofdays(String date){
        int year = Integer.parseInt(date.split("-")[0]);
        if((year%4 == 0 && year%100!= 0) || year%400 == 0){
            return 366;
        }
        return 365;
    }

    /**
     * 获取传入日期的后一天
     * @param date
     * @return
     */
    public static String getDateAfDay1(String date){
        Date d = DateUtil.getDate(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        calendar.add(Calendar.DAY_OF_MONTH,1);
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    /**
     * 获取传入日期的后一年
     * @param date
     * yyyy-MM-dd
     * @return
     */
    public static String getDateAfYear1(String date){
        Date d = DateUtil.getDate(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        calendar.add(Calendar.YEAR,1);
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    /**
     * 获取传入时间多少分钟后的日期与时间
     * @param date 日期(字符串类型，如："yyyy-MM-dd")
     * @param time 时间(字符串类型，如："HH:mm:ss")
     * @param minutes 需要加的分钟数
     * @return  45分钟后的日期，如("yyyy-MM-dd HH:mm:ss")
     */
    public static String getDateDoMinutes(String date,String time,int minutes){
        Calendar calendar = getDatetime(date,time);
        calendar.add(Calendar.MINUTE,minutes);
        Date d = calendar.getTime();
        return getFormatDateTime(d);
    }

    /**
     * 获取传入时间多少天后的日期
     * @param date 传入的日期
     * @param day  需要相加的天数
     * @return day天后的日期，如（"yyyy-MM-dd"）
     */
    public static String getDateDoDay(String date,int day){
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int data = Integer.parseInt(date.split("-")[2]);
        Calendar calendar = new GregorianCalendar(year,month-1,data);
        calendar.add(Calendar.DATE,day);
        Date d = calendar.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    /**
     * 获取传入时间多少天后的日期与时间
     * @param date 日期(字符串类型，如："yyyy-MM-dd")
     * @param day 需要加的天数
     * @return  day天后的日期，如("yyyy-MM-dd EE")
     */
    public static String getDateDoDate(String date,int day){
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int data = Integer.parseInt(date.split("-")[2]);
        Calendar calendar = new GregorianCalendar(year,month-1,data);
        calendar.add(Calendar.DATE,day);
        Date d = calendar.getTime();
        return new SimpleDateFormat("yyyy-MM-dd EE").format(d);
    }

    /**
     * 毫秒数转化为时间
     * @param mss 毫秒(长整形，如：123465873)
     * @return  HH:mm:ss
     */
    public static String getMss2Time(long mss){

        long hour = mss /(60*60*1000);
        long minute = (mss - hour*60*60*1000)/(60*1000);
        long seconds = (mss - hour*60*60*1000 - minute*60*1000)/1000;

        if(seconds >= 60 )
        {
            seconds = seconds % 60;
            minute+=seconds/60;
        }
        if(minute >= 60)
        {
            minute = minute % 60;
            hour  += minute/60;
        }

        String sh = "";
        String sm ="";
        String ss = "";
        if(hour <10) {
            sh = "0" + String.valueOf(hour);
        }else {
            sh = String.valueOf(hour);
        }
        if(minute <10) {
            sm = "0" + String.valueOf(minute);
        }else {
            sm = String.valueOf(minute);
        }
        if(seconds <10) {
            ss = "0" + String.valueOf(seconds);
        }else {
            ss = String.valueOf(seconds);
        }

        return sh +":"+sm+":"+ ss;
    }

    /**
     * 判断日期是否为'yyyy-MM-dd格式'，若不是，则修改为此格式
     * @param date
     * @return
     */
    public static String getDateFormat(String date){
        Date data = getDate(date);
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }

    /**
     * 获取传入日期几年后的日期
     * @param date 日期，（yyyy-MM-dd）
     * @param year 几年
     * @return yyyy-MM-dd
     */
    public static String getFewYearsTime(String date,int year){
        int years = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int data = Integer.parseInt(date.split("-")[2]);
        Calendar calendar = new GregorianCalendar(years,month-1,data);
        calendar.add(Calendar.YEAR,year);
        Date d = calendar.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

}
