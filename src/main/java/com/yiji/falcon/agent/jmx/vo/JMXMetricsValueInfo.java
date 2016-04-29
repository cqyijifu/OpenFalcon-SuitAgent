package com.yiji.falcon.agent.jmx.vo;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/28.
 */

import java.util.List;
import java.util.Map;

/**
 * JMX mBean值的info类
 * Created by QianLong on 16/4/28.
 */
public class JMXMetricsValueInfo {

    /**
     * 此jmx 连接的对象信息
     */
    private List<JMXObjectNameInfo> jmxObjectNameInfoList;

    /**
     * jmx 连接信息
     */
    private JMXConnectionInfo jmxConnectionInfo;

    @Override
    public String toString() {
        return "JMXMetricsValueInfo{" +
                "jmxObjectNameInfoList=" + jmxObjectNameInfoList +
                ", jmxConnectionInfo=" + jmxConnectionInfo +
                '}';
    }

    public JMXConnectionInfo getJmxConnectionInfo() {
        return jmxConnectionInfo;
    }

    public void setJmxConnectionInfo(JMXConnectionInfo jmxConnectionInfo) {
        this.jmxConnectionInfo = jmxConnectionInfo;
    }

    public List<JMXObjectNameInfo> getJmxObjectNameInfoList() {
        return jmxObjectNameInfoList;
    }

    public void setJmxObjectNameInfoList(List<JMXObjectNameInfo> jmxObjectNameInfoList) {
        this.jmxObjectNameInfoList = jmxObjectNameInfoList;
    }
}
