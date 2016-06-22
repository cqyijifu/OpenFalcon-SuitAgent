/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.tomcat;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXMetricsValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class TomcatMetricsValue extends JMXMetricsValue {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取所有的具体服务的JMX监控值VO
     *
     * @return
     */
    @Override
    protected List<JMXMetricsValueInfo> getMetricsValueInfos() {
        return JMXManager.getJmxMetricValue(getServerName(),new TomcatJMXConnection());
    }

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     * @param metricsValueInfo
     * 当前的JMXMetricsValueInfo信息
     * @return
     */
    @Override
    protected Collection<FalconReportObject> getInbuiltReportObjectsForValid(JMXMetricsValueInfo metricsValueInfo) {
        return new ArrayList<>();
    }

    /**
     * 获取step
     *
     * @return
     */
    @Override
    public int getStep() {
        return AgentConfiguration.INSTANCE.getTomcatStep();
    }

    /**
     * 监控类型
     *
     * @return
     */
    @Override
    public String getType() {
        return "tomcat";
    }

    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return
     */
    @Override
    public String getBasePropertiesKey() {
        return "agent.tomcat.metrics.type.";
    }

    /**
     * 自定义的监控属性的配置文件位置
     *
     * @return
     */
    @Override
    public String getMetricsConfPath() {
        return AgentConfiguration.INSTANCE.getTomcatMetricsConfPath();
    }

    /**
     * JMX连接的服务名
     *
     * @return
     */
    @Override
    public String getServerName() {
        return AgentConfiguration.INSTANCE.getTomcatJmxServerName();
    }
}
