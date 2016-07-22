/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo.detect;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 11:49 创建
 */

import java.util.Map;

/**
 * 一次探测的结果
 * @author guqiu@yiji.com
 */
public class DetectResult {

    /**
     * 探测结果,Agent将根据此属性进行探测地址的可用性上报
     */
    private boolean success;

    /**
     * 自定义的监控值
     * Agent会将此Map中的key当做metrics名称,value当做metrics的值进行上报
     */
    private Map<String,Double> metricsMap;

    /**
     * 自定义的tag信息
     * 形如tag1={tag1},tag2={tag2}
     */
    private String tag;

    @Override
    public String toString() {
        return "DetectResult{" +
                "success=" + success +
                ", metricsMap=" + metricsMap +
                ", tag='" + tag + '\'' +
                '}';
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Double> getMetricsMap() {
        return metricsMap;
    }

    public void setMetricsMap(Map<String, Double> metricsMap) {
        this.metricsMap = metricsMap;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
