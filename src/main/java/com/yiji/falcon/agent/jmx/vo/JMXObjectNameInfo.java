package com.yiji.falcon.agent.jmx.vo;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/28.
 */

import javax.management.ObjectName;
import java.util.Map;

/**
 * Created by QianLong on 16/4/28.
 */
public class JMXObjectNameInfo {
    /**
     * 此jmx 连接下的 objectName 的所有监控值
     */
    private Map<String,String> metricsValue;
    /**
     * JMX ObjectName
     */
    private ObjectName objectName;
    /**
     * 监控连接信息
     */
    private JMXConnectionInfo jmxConnectionInfo;

    @Override
    public String toString() {
        return "JMXObjectNameInfo{" +
                "metricsValue=" + metricsValue +
                ", objectName=" + objectName +
                ", jmxConnectionInfo=" + jmxConnectionInfo +
                '}';
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public JMXObjectNameInfo() {
    }

    public JMXConnectionInfo getJmxConnectionInfo() {
        return jmxConnectionInfo;
    }

    public void setJmxConnectionInfo(JMXConnectionInfo jmxConnectionInfo) {
        this.jmxConnectionInfo = jmxConnectionInfo;
    }

    public Map<String, String> getMetricsValue() {
        return metricsValue;
    }

    public void setMetricsValue(Map<String, String> metricsValue) {
        this.metricsValue = metricsValue;
    }
}
