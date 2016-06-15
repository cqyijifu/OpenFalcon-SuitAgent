package com.yiji.falcon.agent.plugins.yijiBoot;/**
 * Copyright 2016-2017 the original ql
 * Created by QianLong on 16/6/15.
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXMetricsValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by QianLong on 16/6/15.
 */
public class YijiBootMetricsValue extends JMXMetricsValue {

    //yijiBoot的jar文件名称,也是yijiBoot的JMC连接名
    private String appJarName;

    /**
     * 需单独指定相应的yijiBoot应用的jar文件名称
     * @param appJarName
     */
    public YijiBootMetricsValue(String appJarName) {
        this.appJarName = appJarName;
    }

    /**
     * 获取所有的具体服务的JMX监控值VO
     *
     * @return
     */
    @Override
    protected List<JMXMetricsValueInfo> getMetricsValueInfos() {
        return JMXManager.getJmxMetricValue(getServerName(),new YijiBootJMXConnection(getAppJarName().replace(".jar","")));
    }

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     *
     * @param metricsValueInfo 当前的JMXMetricsValueInfo信息
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
        return AgentConfiguration.INSTANCE.getYijiBootStep();
    }

    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return
     */
    @Override
    public String getBasePropertiesKey() {
        return null;
    }

    /**
     * 自定义的监控属性的配置文件位置
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
        return getAppJarName();
    }

    /**
     * 监控类型
     *
     * @return
     */
    @Override
    public String getType() {
        return "yijiBoot";
    }

    public String getAppJarName() {
        return appJarName;
    }
}
