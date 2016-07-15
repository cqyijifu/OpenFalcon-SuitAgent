/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.netDevice;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-14 10:12 创建
 */

import com.yiji.falcon.agent.plugins.util.SNMPV3Session;

import java.util.Date;

/**
 * @author guqiu@yiji.com
 */
class CollectObject {
    private String metrics;
    private String value;
    private Date time;
    private SNMPV3Session session;

    @Override
    public String toString() {
        return "CollectObject{" +
                "metrics='" + metrics + '\'' +
                ", value='" + value + '\'' +
                ", time=" + time +
                ", session=" + session +
                '}';
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public SNMPV3Session getSession() {
        return session;
    }

    public void setSession(SNMPV3Session session) {
        this.session = session;
    }
}
