/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.falcon;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
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
