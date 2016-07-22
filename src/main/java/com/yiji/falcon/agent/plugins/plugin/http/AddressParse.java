/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.http;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 14:53 创建
 */

/**
 * 配置文件的地址解析
 * @author guqiu@yiji.com
 */
public class AddressParse {

    /**
     * 地址对象
     */
    static class Address{
        public String method;
        public String protocol;
        public String url;

        Address(String method, String protocol, String url) {
            this.method = method;
            this.protocol = protocol;
            this.url = url;
        }

        boolean isGetMethod(){
            return "get".equalsIgnoreCase(method);
        }

        boolean isPostMethod(){
            return "post".equalsIgnoreCase(method);
        }

        boolean isHttp(){
            return "http".equalsIgnoreCase(protocol);
        }

        boolean isHttps(){
            return "https".equalsIgnoreCase(protocol);
        }

    }

    /**
     * 根据地址字符串配置解析地址,返回地址对象
     * @param address
     * method:protocol:url格式的地址
     * @return
     * 解析失败返回null
     */
    public static Address parseAddress(String address){
        String[] ss = address.split(":");
        if(ss.length != 3){
            return null;
        }
        return new Address(ss[0],ss[1],ss[2]);
    }

}
