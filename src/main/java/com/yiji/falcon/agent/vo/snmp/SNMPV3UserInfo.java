/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo.snmp;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-11 14:51 创建
 */

import java.util.List;

/**
 * SNMPV3协议访问的授权用户信息
 * @author guqiu@yiji.com
 */
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

    @Override
    public String toString() {
        return "SNMPV3UserInfo{" +
                "protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", port='" + port + '\'' +
                ", username='" + username + '\'' +
                ", aythType='" + aythType + '\'' +
                ", authPswd='" + authPswd + '\'' +
                ", privType='" + privType + '\'' +
                ", privPswd='" + privPswd + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", ifCollectNameEnables='" + ifCollectNameEnables + '\'' +
                '}';
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAythType() {
        return aythType;
    }

    public void setAythType(String aythType) {
        this.aythType = aythType;
    }

    public String getAuthPswd() {
        return authPswd;
    }

    public void setAuthPswd(String authPswd) {
        this.authPswd = authPswd;
    }

    public String getPrivType() {
        return privType;
    }

    public void setPrivType(String privType) {
        this.privType = privType;
    }

    public String getPrivPswd() {
        return privPswd;
    }

    public void setPrivPswd(String privPswd) {
        this.privPswd = privPswd;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public List<String> getIfCollectNameEnables() {
        return ifCollectNameEnables;
    }

    public void setIfCollectNameEnables(List<String> ifCollectNameEnables) {
        this.ifCollectNameEnables = ifCollectNameEnables;
    }
}
