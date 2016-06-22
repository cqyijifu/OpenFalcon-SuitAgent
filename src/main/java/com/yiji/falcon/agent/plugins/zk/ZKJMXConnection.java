/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.zk;

import com.yiji.falcon.agent.jmx.JMXConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import java.io.IOException;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class ZKJMXConnection extends JMXConnection {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 获取指定连接对监控平台暴露标签名
     *
     * @param mBeanServerConnection
     * @return
     */
    @Override
    public String getJmxConnectionName(MBeanServerConnection mBeanServerConnection,int pid) {
        try {
            return String.valueOf(ZKConfig.getClientPort(pid));
        } catch (IOException e) {
            log.error("获取zookeeper clientPort 信息失败",e);
            return "";
        }
    }
}
