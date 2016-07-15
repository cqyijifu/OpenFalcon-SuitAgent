/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.falcon;

import com.yiji.falcon.agent.util.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.management.ObjectName;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * push到falcon的数据报告对象
 * @author guqiu@yiji.com
 */
public class FalconReportObject implements Cloneable{

    /**
     *  标明Metric的主体(属主)，比如metric是cpu_idle，那么Endpoint就表示这是哪台机器的cpu_idle
     */
    private String endpoint;
    /**
     * 最核心的字段，代表这个采集项具体度量的是什么, 比如是cpu_idle呢，还是memory_free, 还是qps
     */
    private String metric;
    /**
     * 表示汇报该数据时的unix时间戳，注意是整数，代表的是秒
     */
    private long timestamp;
    /**
     * 表示该数据采集项的汇报周期，这对于后续的配置监控策略很重要，必须明确指定。
     */
    private int step;
    /**
     * 代表该metric在当前时间点的值,数值类型
     */
    private String value;
    /**
     * 只能是COUNTER或者GAUGE二选一，前者表示该数据采集项为计时器类型，后者表示其为原值 (注意大小写)
     GAUGE：即用户上传什么样的值，就原封不动的存储
     COUNTER：指标在存储和展现的时候，会被计算为speed，即（当前值 - 上次值）/ 时间间隔
     */
    private CounterType counterType;
    /**
     * 一组逗号分割的键值对, 对metric进一步描述和细化, 可以是空字符串. 比如idc=lg，比如service=xbox等，多个tag之间用逗号分割
     */
    private String tags;

    /**
     * 仅供系统使用
     */
    private ObjectName objectName;

    @Override
    public FalconReportObject clone() {
        try {
            return (FalconReportObject) super.clone();
        } catch (CloneNotSupportedException e) {
            return new FalconReportObject();
        }
    }

    @Override
    public String toString() {
        return "FalconReportObject{" +
                "endpoint='" + endpoint + '\'' +
                ", metric='" + metric + '\'' +
                ", timestamp=" + timestamp +
                ", step=" + step +
                ", value=" + value +
                ", counterType=" + counterType +
                ", tags='" + tags + '\'' +
                ", objectName=" + objectName +
                '}';
    }

    /**
     * 添加tag
     * @param newTag
     * @return
     */
    public FalconReportObject appendTags(String newTag){
        if(StringUtils.isEmpty(this.getTags())){
            this.setTags(newTag);
        }else{
            this.setTags(this.getTags() + "," + newTag);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof FalconReportObject)) return false;

        FalconReportObject that = (FalconReportObject) o;

        return new EqualsBuilder()
                .append(timestamp, that.timestamp)
                .append(step, that.step)
                .append(value, that.value)
                .append(endpoint, that.endpoint)
                .append(metric, that.metric)
                .append(counterType, that.counterType)
                .append(tags, that.tags)
                .append(objectName, that.objectName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(endpoint)
                .append(metric)
                .append(timestamp)
                .append(step)
                .append(value)
                .append(counterType)
                .append(tags)
                .append(objectName)
                .toHashCode();
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CounterType getCounterType() {
        return counterType;
    }

    public void setCounterType(CounterType counterType) {
        this.counterType = counterType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
