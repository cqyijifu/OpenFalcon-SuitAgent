package com.yiji.falcon.agent.plugins.logstash;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/30.
 */

import com.yiji.falcon.agent.jmx.JMXConnection;

import javax.management.MBeanServerConnection;

/**
 * Created by QianLong on 16/5/30.
 */
public class LogstashJMXConnection extends JMXConnection {
    /**
     * 获取指定连接对监控平台暴露标签名
     *
     * @param mBeanServerConnection
     * @param pid
     * @return
     */
    @Override
    public String getJmxConnectionName(MBeanServerConnection mBeanServerConnection, int pid) {
        return null;
    }
}
