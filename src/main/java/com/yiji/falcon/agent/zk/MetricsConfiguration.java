package com.yiji.falcon.agent.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/29.
 */

/**
 * Agent配置文件配置的需要进行获取的监控参数
 * Created by QianLong on 16/4/29.
 */
public class MetricsConfiguration {

    private String objectName;
    private String metrics;
    private String counterType;
    private String tag;

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
}
