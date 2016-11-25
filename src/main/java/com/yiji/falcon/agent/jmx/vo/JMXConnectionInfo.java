/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx.vo;

import com.yiji.falcon.agent.jmx.JMXManager;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JMXConnectionInfo)) return false;

        JMXConnectionInfo that = (JMXConnectionInfo) o;

        return new EqualsBuilder()
                .append(pid, that.pid)
                .append(valid, that.valid)
                .append(cacheKeyId, that.cacheKeyId)
                .append(mBeanServerConnection, that.mBeanServerConnection)
                .append(connectionQualifiedServerName, that.connectionQualifiedServerName)
                .append(connectionServerName, that.connectionServerName)
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(cacheKeyId)
                .append(mBeanServerConnection)
                .append(connectionQualifiedServerName)
                .append(connectionServerName)
                .append(name)
                .append(pid)
                .append(valid)
                .toHashCode();
    }

    public String getConnectionServerName() {
        return connectionServerName;
    }

    public void setConnectionServerName(String connectionServerName) {
        this.connectionServerName = connectionServerName;
    }

    public String getCacheKeyId() {
        return cacheKeyId;
    }

    public void setCacheKeyId(String cacheKeyId) {
        this.cacheKeyId = cacheKeyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * 注：请勿使用此对象进行JMX连接交互操作，可能会在JMX维护时发生java.io.IOException: The client has been closed异常 （{@link JMXManager#getJmxMetricValue(java.lang.String)} 方法除外）
     * @return
     */
    public MBeanServerConnection getmBeanServerConnection() {
        return mBeanServerConnection;
    }

    public void setmBeanServerConnection(MBeanServerConnection mBeanServerConnection) {
        this.mBeanServerConnection = mBeanServerConnection;
    }

    public String getConnectionQualifiedServerName() {
        return connectionQualifiedServerName;
    }

    public void setConnectionQualifiedServerName(String connectionQualifiedServerName) {
        this.connectionQualifiedServerName = connectionQualifiedServerName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public JMXConnector getJmxConnector() {
        return jmxConnector;
    }

    public void setJmxConnector(JMXConnector jmxConnector) {
        this.jmxConnector = jmxConnector;
    }
}
