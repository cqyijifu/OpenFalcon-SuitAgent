/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-12 15:28 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class JMXConnectUrlInfo {
    /**
     * 是否需要认证
     */
    private boolean isAuthentication;
    /**
     * JMX Remote连接地址
     */
    private String remoteUrl;
    /**
     * JMX连接用户
     */
    private String jmxUser;
    /**
     * JMX连接密码
     */
    private String jmxPassword;

    public JMXConnectUrlInfo() {
    }

    public JMXConnectUrlInfo(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    @Override
    public String toString() {
        return "JMXRemoteUrlInfo{" +
                "isAuthentication=" + isAuthentication +
                ", remoteUrl='" + remoteUrl + '\'' +
                ", jmxUser='" + jmxUser + '\'' +
                ", jmxPassword='" + jmxPassword + '\'' +
                '}';
    }

    public boolean isAuthentication() {
        return isAuthentication;
    }

    public void setAuthentication(boolean authentication) {
        isAuthentication = authentication;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getJmxUser() {
        return jmxUser;
    }

    public void setJmxUser(String jmxUser) {
        this.jmxUser = jmxUser;
    }

    public String getJmxPassword() {
        return jmxPassword;
    }

    public void setJmxPassword(String jmxPassword) {
        this.jmxPassword = jmxPassword;
    }

}
