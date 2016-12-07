/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-22 14:01 创建
 */

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class MD5Util {

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
            log.error("",e);
        }

        return null;

    }

}
