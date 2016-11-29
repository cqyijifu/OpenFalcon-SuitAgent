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
    JMX_OBJECT_CONF("jmxObjectConf"),
    /**
     * 內建的JMX公共的监控属性
     */
    JMX_OBJECT_IN_BUILD("jmxObjectInBuild"),
    /**
     * 配置文件配置的通过http访问获取的监控值
     */
    HTTP_URL_CONF("httpUrlConf"),
    /**
     * 配置文件配置的SQL语句查询的监控值
     */
    SQL_CONF("sqlConf"),
    /**
     * 內建的SQL语句查询的监控值
     */
    SQL_IN_BUILD("sqlInBuild"),
    /**
     * 內建的公共的SNMP获取的监控值
     */
    SNMP_COMMON_IN_BUILD("snmpCommonInBuild"),
    /**
     * 插件內建的SNMP监控值
     */
    SNMP_Plugin_IN_BUILD("snmpPluginInBuild"),
    DETECT("detect");

    private String typeName;

    MetricsType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
