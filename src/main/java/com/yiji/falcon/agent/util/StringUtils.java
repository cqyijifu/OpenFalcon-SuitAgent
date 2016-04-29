package com.yiji.falcon.agent.util;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/27.
 */

/**
 * Created by QianLong on 16/4/27.
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

}
