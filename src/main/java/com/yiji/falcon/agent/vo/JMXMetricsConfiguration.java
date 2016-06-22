/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * JMX监控方式 Agent配置文件配置的需要进行获取的监控参数
 * @author guqiu@yiji.com
 */
public class JMXMetricsConfiguration {

    private String objectName;
    private String metrics;
    private String valueExpress;
    private String alias;
    private String counterType;
    private String tag;

    @Override
    public String toString() {
        return "JMXMetricsConfiguration{" +
                "objectName='" + objectName + '\'' +
                ", metrics='" + metrics + '\'' +
                ", valueExpress='" + valueExpress + '\'' +
                ", alias='" + alias + '\'' +
                ", counterType='" + counterType + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JMXMetricsConfiguration)) return false;

        JMXMetricsConfiguration that = (JMXMetricsConfiguration) o;

        return new EqualsBuilder()
                .append(objectName, that.objectName)
                .append(metrics, that.metrics)
                .append(valueExpress, that.valueExpress)
                .append(alias, that.alias)
                .append(counterType, that.counterType)
                .append(tag, that.tag)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(objectName)
                .append(metrics)
                .append(valueExpress)
                .append(alias)
                .append(counterType)
                .append(tag)
                .toHashCode();
    }

    public String getValueExpress() {
        return valueExpress;
    }

    public void setValueExpress(String valueExpress) {
        this.valueExpress = valueExpress;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getCounterType() {
        return counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
