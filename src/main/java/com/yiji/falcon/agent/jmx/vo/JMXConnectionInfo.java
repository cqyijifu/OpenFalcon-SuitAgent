/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx.vo;

import com.yiji.falcon.agent.jmx.JMXManager;
import lombok.Data;
import lombok.Getter;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
@Data
public class JMXConnectionInfo {
    private String cacheKeyId;//JMX缓存的key的唯一id
    private MBeanServerConnection mBeanServerConnection;
    private JMXConnector jmxConnector;
    private String connectionQualifiedServerName;//JMX中的jmx连接限定名
    private String connectionServerName;//配置中指定的服务名
    private String name;//此jmx连接对监控展示的名称
    private int pid;//jmx的进程号
    private boolean valid;

    @Override
    public String toString() {
        return "JMXConnectionInfo{" +
                "cacheKeyId='" + cacheKeyId + '\'' +
                ", mBeanServerConnection=" + mBeanServerConnection +
                ", jmxConnector=" + jmxConnector +
                ", connectionQualifiedServerName='" + connectionQualifiedServerName + '\'' +
                ", connectionServerName='" + connectionServerName + '\'' +
                ", name='" + name + '\'' +
                ", pid=" + pid +
                ", valid=" + valid +
                '}';
    }

    /**
     * 注：请勿使用此对象进行JMX连接交互操作，可能会在JMX维护时发生java.io.IOException: The client has been closed异常 （{@link JMXManager#getJmxMetricValue(java.lang.String)} 方法除外）
     * @return
     */
    public MBeanServerConnection getmBeanServerConnection() {
        return mBeanServerConnection;
    }
}
