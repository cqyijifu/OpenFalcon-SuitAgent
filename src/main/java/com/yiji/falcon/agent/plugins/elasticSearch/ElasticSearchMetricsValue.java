package com.yiji.falcon.agent.plugins.elasticSearch;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/24.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXMetricsValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by QianLong on 16/5/24.
 */
public class ElasticSearchMetricsValue extends JMXMetricsValue {
    /**
     * 获取所有的具体服务的JMX监控值VO
     *
     * @return
     */
    @Override
    protected List<JMXMetricsValueInfo> getMetricsValueInfos() {
        return JMXManager.getJmxMetricValue(getServerName(),new ElasticSearchJMXConnection());
    }

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     *
     * @return
     */
    @Override
    protected Collection<FalconReportObject> getInbuiltReportObjectsForValid() {
        return new ArrayList<>();
    }

    /**
     * 获取step
     *
     * @return
     */
    @Override
    public int getStep() {
        return AgentConfiguration.INSTANCE.getElasticSearchStep();
    }

    /**
     * 监控类型
     *
     * @return
     */
    @Override
    public String getType() {
        return "elasticSearch";
    }

    /**
     * 监控值配置项基础配置名
     *
     * @return
     */
    @Override
    public String getBasePropertiesKey() {
        return null;
    }

    /**
     * 监控属性的配置文件位置
     *
     * @return
     */
    @Override
    public String getMetricsConfPath() {
        return null;
    }

    /**
     * JMX连接的服务名
     *
     * @return
     */
    @Override
    public String getServerName() {
        return AgentConfiguration.INSTANCE.getElasticSearchJmxServerName();
    }
}
