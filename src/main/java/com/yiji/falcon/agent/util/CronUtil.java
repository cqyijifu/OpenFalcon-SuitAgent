/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import java.util.Calendar;
import java.util.Date;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * Cron工具类
 * @author guqiu@yiji.com
 */
public class CronUtil {

    /**
     * 获取以指定秒数为单位的定时调度
     * @param second
     * 最小不能小于0
     * 最大不能超过 24 * 60 * 60 秒
     * @return
     * null 获取失败
     */
    public static String getCronBySecondScheduler(int second){
        if(second >= (24 * 60 * 60) || second == 0){
            return null;
        }
        int hh = 0;
        if(second >= 3600){
            hh = second / 3600;
            second = second - ( hh * 3600);
        }
        int mm = second/60;
        int ss = second%60;
        return (ss == 0 ? "0" : ("*/" + ss)) + (mm == 0 ? (ss == 0 ? (hh == 0 ? (" *") : " 0") : (hh == 0 ? " *" : " 0")) : (" */" + mm)) + (hh == 0 ? " *" : (" */" + hh)) + " * * ?";
    }

    /**
     * 获取传入日期，时间的cron表达式
     * @param date 传入的日期
     * @param time 传入的时间
     * @return
     */
    public static String getCron(String date,String time){
        Calendar calendar = DateUtil.getDatetime(date,time);
        Date d = calendar.getTime();
        String dateStr = DateUtil.getFormatDateTime(d);
        String[] str = dateStr.split(" ");

        int year = Integer.parseInt(str[0].split("-")[0]);
        int month = Integer.parseInt(str[0].split("-")[1]);
        int day = Integer.parseInt(str[0].split("-")[2]);

        int HH = Integer.parseInt(str[1].split(":")[0]);
        int mm = Integer.parseInt(str[1].split(":")[1]);
        int ss = Integer.parseInt(str[1].split(":")[2]);
        return ss + " " + mm + " " + HH + " " + day + " " + month + " " + "?" + " " + year;
    }

    /**
     * 获取传入日期，时间前多少分钟的cron表达式
     * @param date 传入的日期，如："yyyy-MM-dd"
     * @param time 传入的时间，如：'hh:mm:ss'
     * @param minutes 需要减的分钟
     * @return
     */
    public static String getCronExMinutes(String date,String time,int minutes){
        Calendar calendar = DateUtil.getDatetime(date,time);
        calendar.add(Calendar.MINUTE,-minutes);
        Date d = calendar.getTime();
        String dateStr = DateUtil.getFormatDateTime(d);
        String[] str = dateStr.split(" ");

        int year = Integer.parseInt(str[0].split("-")[0]);
        int month = Integer.parseInt(str[0].split("-")[1]);
        int day = Integer.parseInt(str[0].split("-")[2]);

        int HH = Integer.parseInt(str[1].split(":")[0]);
        int mm = Integer.parseInt(str[1].split(":")[1]);
        int ss = Integer.parseInt(str[1].split(":")[2]);
        return ss + " " + mm + " " + HH + " " + day + " " + month + " " + "?" + " " + year;
    }

    /**
     * 获取传入日期，时间后多少分钟的cron表达式
     * @param date 传入的日期，如："yyyy-MM-dd"
     * @param time 传入的时间，如：'hh:mm:ss'
     * @param minutes 需要加的分钟
     * @return cron表达式
     */
    public static String getCronDoMinutes(String date,String time,int minutes){
        Calendar calendar = DateUtil.getDatetime(date,time);
        calendar.add(Calendar.MINUTE,minutes);
        Date d = calendar.getTime();
        String dateStr = DateUtil.getFormatDateTime(d);
        String[] str = dateStr.split(" ");

        int year = Integer.parseInt(str[0].split("-")[0]);
        int month = Integer.parseInt(str[0].split("-")[1]);
        int day = Integer.parseInt(str[0].split("-")[2]);

        int HH = Integer.parseInt(str[1].split(":")[0]);
        int mm = Integer.parseInt(str[1].split(":")[1]);
        int ss = Integer.parseInt(str[1].split(":")[2]);
        return ss + " " + mm + " " + HH + " " + day + " " + month + " " + "?" + " " + year;
    }

    /**
     * 获取传入日期与时间后几天的cron表达式
     * @param date 传入的日期，如："yyyy-MM-dd"
     * @param time 传入的时间，如："HH:mm:ss"
     * @param day  需要加的天数
     * @return
     */
    public static String getCronAfDay(String date,String time,int day){
        Calendar calendar = DateUtil.getDatetime(date,time);
        calendar.add(Calendar.DATE,day);
        Date d = calendar.getTime();
        String dateStr = DateUtil.getFormatDateTime(d);
        String[] str = dateStr.split(" ");

        int year = Integer.parseInt(str[0].split("-")[0]);
        int month = Integer.parseInt(str[0].split("-")[1]);
        int toDay = Integer.parseInt(str[0].split("-")[2]);

        int HH = Integer.parseInt(str[1].split(":")[0]);
        int mm = Integer.parseInt(str[1].split(":")[1]);
        int ss = Integer.parseInt(str[1].split(":")[2]);

        return ss + " " + mm + " " + HH + " " + toDay + " " + month + " " + "?" + " " + year;
    }


}
