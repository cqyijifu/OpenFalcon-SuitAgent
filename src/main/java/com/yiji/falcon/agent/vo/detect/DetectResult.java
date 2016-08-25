/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo.detect;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 11:49 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;

import java.util.List;

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
     * Agent会将此Map中的key当做metrics名称,{@link com.yiji.falcon.agent.vo.detect.DetectResult.Metric} 对象组建上报值进行上报
     */
    private List<Metric> metricsList;


    /**
     * 自定义的公共的tag信息
     * 形如tag1={tag1},tag2={tag2}
     * 设置后,将会对每个监控值都会打上此tag
     */
    private String commonTag;
    @Override
    public String toString() {
        return "DetectResult{" +
                "success=" + success +
                ", metricsList=" + metricsList +
                ", commonTag='" + commonTag + '\'' +
                '}';
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCommonTag() {
        return commonTag;
    }

    public void setCommonTag(String commonTag) {
        this.commonTag = commonTag;
    }

    public List<Metric> getMetricsList() {
        return metricsList;
    }

    public void setMetricsList(List<Metric> metricsList) {
        this.metricsList = metricsList;
    }

    public static class Metric{
        /**
         * 自定义监控名
         */
        public String metricName;
        /**
         * 自定义监控值的value
         */
        public String value;
        /**
         * 自定义监控值的上报类型
         */
        public CounterType counterType;
        /**
         * 自定义监控值的tag
         */
        public String tags;

        /**
         * @param metricName
         * @param value
         * @param counterType
         * @param tags
         */
        public Metric(String metricName,String value, CounterType counterType, String tags) {
            this.metricName = metricName;
            this.value = value;
            this.counterType = counterType;
            this.tags = tags;
        }

        @Override
        public String toString() {
            return "Metric{" +
                    "metricName='" + metricName + '\'' +
                    ", value=" + value +
                    ", counterType=" + counterType +
                    ", tags='" + tags + '\'' +
                    '}';
        }
    }
}
