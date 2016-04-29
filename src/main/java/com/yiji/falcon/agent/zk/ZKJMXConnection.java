package com.yiji.falcon.agent.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/29.
 */

import com.yiji.falcon.agent.jmx.JMXConnection;

import javax.management.MBeanServerConnection;

/**
 * Created by QianLong on 16/4/29.
 */
public class ZKJMXConnection extends JMXConnection {
    /**
     * 获取指定连接对监控平台暴露标签名
     *
     * @param mBeanServerConnection
     * @return
     */
    @Override
    public String getJmxConnectionName(MBeanServerConnection mBeanServerConnection) {
        return "";
    }
}
