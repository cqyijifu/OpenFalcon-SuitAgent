/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx.vo;

import javax.management.ObjectName;
import java.util.Map;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class JMXObjectNameInfo {
    /**
     * 此jmx 连接下的 objectName 的所有监控值
     */
    private Map<String,Object> metricsValue;
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

    public Map<String, Object> getMetricsValue() {
        return metricsValue;
    }

    public void setMetricsValue(Map<String, Object> metricsValue) {
        this.metricsValue = metricsValue;
    }
}
