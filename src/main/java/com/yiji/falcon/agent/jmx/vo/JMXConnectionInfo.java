package com.yiji.falcon.agent.jmx.vo;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/28.
 */

import javax.management.MBeanServerConnection;

/**
 * JMX连接信息
 * Created by QianLong on 16/4/28.
 */
public class JMXConnectionInfo {
    private MBeanServerConnection mBeanServerConnection;
    private String connectionQualifiedServerName;//JMX中的jmx连接限定名
    private String connectionServerName;//配置中指定的服务名
    private String name;//此jmx连接对监控展示的名称
    private boolean valid;

    @Override
    public String toString() {
        return "JMXConnectionInfo{" +
                "mBeanServerConnection=" + mBeanServerConnection +
                ", connectionQualifiedServerName='" + connectionQualifiedServerName + '\'' +
                ", connectionServerName='" + connectionServerName + '\'' +
                ", name='" + name + '\'' +
                ", valid=" + valid +
                '}';
    }

    public String getConnectionServerName() {
        return connectionServerName;
    }

    public void setConnectionServerName(String connectionServerName) {
        this.connectionServerName = connectionServerName;
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
}
