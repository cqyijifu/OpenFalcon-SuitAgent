/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.yijiBoot;

import com.yiji.falcon.agent.jmx.JMXConnection;

import javax.management.MBeanServerConnection;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class YijiBootJMXConnection extends JMXConnection {

    //yijiBoot的应用名称
    private String appName;

    /**
     * 需指定yijiBoot的应用名称
     * @param appName
     */
    public YijiBootJMXConnection(String appName) {
        this.appName = appName;
    }

    /**
     * 获取指定连接对监控平台暴露标签名
     *
     * @param mBeanServerConnection
     * @param pid
     * @return
     */
    @Override
    public String getJmxConnectionName(MBeanServerConnection mBeanServerConnection, int pid) {
        return getAppName();
    }

    public String getAppName() {
        return appName;
    }
}
