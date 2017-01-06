/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx.vo;

import com.yiji.falcon.agent.exception.JMXUnavailabilityType;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.util.ExecuteThreadUtil;
import lombok.Getter;
import lombok.Setter;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class JMXConnectionInfo {
    @Getter
    @Setter
    private String cacheKeyId;//JMX缓存的key的唯一id

    @Setter
    private MBeanServerConnection mBeanServerConnection;

    @Getter
    @Setter
    private JMXConnector jmxConnector;

    @Getter
    @Setter
    private String connectionQualifiedServerName;//JMX中的jmx连接限定名

    @Getter
    @Setter
    private String connectionServerName;//配置中指定的服务名

    @Getter
    @Setter
    private String name;//此jmx连接对监控展示的名称

    @Getter
    @Setter
    private int pid;//jmx的进程号

    private boolean valid;

    /**
     * 不可用类型
     * 注：当type为{@link JMXUnavailabilityType#connectionFailed}时，该对象中的{@link JMXConnectionInfo#jmxConnector}才是真正的不可用，其他情况，应该正常进行采集操作
     */
    @Getter
    private JMXUnavailabilityType type;

    /**
     * 关闭JMX连接
     */
    public void closeJMXConnector(){
        ExecuteThreadUtil.execute(() -> {
            if(jmxConnector != null){
                try {
                    jmxConnector.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * 设置不可用时自动关闭JMX连接
     * @param valid
     */
    public void setValid(boolean valid,JMXUnavailabilityType type) {
        this.valid = valid;
        if(!valid){
            this.type = type;
            closeJMXConnector();
        }
    }

    @Override
    public String toString() {
        return "JMXConnectionInfo{" +
                "cacheKeyId='" + cacheKeyId + '\'' +
                ", mBeanServerConnection=" + mBeanServerConnection +
                ", jmxConnector=" + jmxConnector +
                ", connectionQualifiedServerName='" + connectionQualifiedServerName + '\'' +
                ", connectionServerName='" + connectionServerName + '\'' +
                ", name='" + name + '\'' +
                ", pid=" + pid +
                ", valid=" + valid +
                ", JMXUnavailabilityType=" + type +
                '}';
    }

    /**
     * 注：请勿使用此对象进行JMX连接交互操作，可能会在JMX维护时发生java.io.IOException: The client has been closed异常 （{@link JMXManager#getJmxMetricValue(java.lang.String)} 方法除外）
     * @return
     */
    public MBeanServerConnection getmBeanServerConnection() {
        return mBeanServerConnection;
    }
}
