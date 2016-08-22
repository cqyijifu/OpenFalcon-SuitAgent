/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-22 14:01 创建
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;

/**
 * @author guqiu@yiji.com
 */
public class MD5Util {

    private static final Logger logger = LoggerFactory.getLogger(MD5Util.class);

    /**
     * 对传入的字符串进行MD5加密并用base64重新编码
     * 获取失败将传回null
     * @param str
     * @return
     */
    public static String getMD5(String str){

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64 = new BASE64Encoder();
            return base64.encode(md5.digest(str.getBytes("utf-8")));
        } catch (Exception e) {
            logger.error("",e);
        }

        return null;

    }

}
