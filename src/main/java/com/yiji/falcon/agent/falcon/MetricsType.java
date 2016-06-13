package com.yiji.falcon.agent.falcon;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/6/13.
 */

/**
 * Created by QianLong on 16/6/13.
 */
public enum MetricsType {

    /**
     * 可用性监控值
     */
    AVAILABILITY("availability"),
    /**
     * 配置文件配置的JMX监控值
     */
    JMXOBJECTCONF("jmxObjectConf"),
    /**
     * 內建的JMX公共的监控属性
     */
    JMXOBJECTBUILDIN("jmxObjectBuildIn"),
    /**
     * 配置文件配置的通过http访问获取的监控值
     */
    HTTPURLCONF("httpUrlConf"),
    /**
     * 配置文件配置的SQL语句查询的监控值
     */
    SQLCONF("sqlConf"),
    /**
     * 內建的SQL语句查询的监控值
     */
    SQLBUILDIN("sqlBuildIn");

    private String typeName;

    MetricsType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
