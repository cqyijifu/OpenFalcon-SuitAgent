/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class StringUtils {

    /**
     * 判断传入的String是否为空('' or null)
     * @param str
     * @return
     */
    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }

    /**
     * 返回指定数字的字符串形式
     * @param value
     * @return
     */
    public static String getStringByInt(int value){
        return String.valueOf(value).intern();
    }

}
