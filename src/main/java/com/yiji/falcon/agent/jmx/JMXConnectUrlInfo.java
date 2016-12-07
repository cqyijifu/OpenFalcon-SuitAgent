/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-12 15:28 创建
 */

import lombok.Data;

/**
 * @author guqiu@yiji.com
 */
@Data
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

}
