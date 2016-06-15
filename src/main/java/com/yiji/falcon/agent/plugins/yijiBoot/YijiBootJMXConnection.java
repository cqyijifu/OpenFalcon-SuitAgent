package com.yiji.falcon.agent.plugins.yijiBoot;/**
 * Copyright 2016-2017 the original ql
 * Created by QianLong on 16/6/15.
 */

import com.yiji.falcon.agent.jmx.JMXConnection;

import javax.management.MBeanServerConnection;

/**
 * Created by QianLong on 16/6/15.
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
