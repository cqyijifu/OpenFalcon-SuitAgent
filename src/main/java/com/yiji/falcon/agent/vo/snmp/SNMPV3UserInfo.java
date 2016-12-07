/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo.snmp;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-11 14:51 创建
 */

import lombok.Data;

import java.util.List;

/**
 * SNMPV3协议访问的授权用户信息
 * @author guqiu@yiji.com
 */
@Data
public class SNMPV3UserInfo {

    /**
     * 网络协议 如udp、tcp
     */
    private String protocol = "udp";
    /**
     * 网络设备的IP地址
     */
    private String address = "";
    /**
     * 网络设备的连接端口号
     */
    private String port = "161";
    /**
     * 用户名
     */
    private String username = "";
    /**
     * 认证算法,如 none,MD5,SHA
     */
    private String aythType = "";
    /**
     * 认证密码
     */
    private String authPswd = "";
    /**
     * 加密算法,如 none,DES,3DES,AES128,AES,AES192,AES256
     */
    private String privType = "";
    /**
     * 加密密码
     */
    private String privPswd = "";
    /**
     * endPoint
     */
    private String endPoint = "";

    /**
     * 允许采集哪些名称的接口数据
     * 注：只采集集合中的接口名称的数据。若为空，则不会采集接口数据
     */
    private List<String> ifCollectNameEnables;

}
