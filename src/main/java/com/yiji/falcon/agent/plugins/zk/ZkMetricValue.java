package com.yiji.falcon.agent.plugins.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.jmx.vo.JMXObjectNameInfo;
import com.yiji.falcon.agent.plugins.JMXMetricsValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by QianLong on 16/4/25.
 */
public class ZkMetricValue extends JMXMetricsValue {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取所有的具体服务的JMX监控值VO
     *
     * @return
     */
    @Override
    protected List<JMXMetricsValueInfo> getMetricsValueInfos() {
        return JMXManager.getJmxMetricValue(getServerName(),new ZKJMXConnection());
    }

    /**
     * 当可用时的內建监控报告
     * @param metricsValueInfo
     * 当前的JMXMetricsValueInfo信息
     * @return
     */
    @Override
    public Collection<FalconReportObject> getInbuiltReportObjectsForValid(JMXMetricsValueInfo metricsValueInfo) {
        boolean isLeader = false;
        String name = metricsValueInfo.getJmxConnectionInfo().getName();
        List<FalconReportObject> result = new ArrayList<>();
        for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
            if(objectNameInfo.toString().contains("Leader")){
                //若ObjectName中包含有 Leader 则该zk为Leader角色
                isLeader = true;
            }
        }
        result.add(generatorIsLeaderReport(isLeader,name));
        return result;
    }

    private FalconReportObject generatorIsLeaderReport(boolean isLeader,String name){
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric(getMetricsName("isLeader",name));
        falconReportObject.setValue(isLeader ? "1" : "0");
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        return falconReportObject;
    }

    /**
     * 获取step
     *
     * @return
     */
    @Override
    public int getStep() {
        return AgentConfiguration.INSTANCE.getZkStep();
    }

    /**
     * 监控类型
     *
     * @return
     */
    @Override
    public String getType() {
        return "zookeeper";
    }


    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return
     */
    @Override
    public String getBasePropertiesKey() {
        return "agent.zk.metrics.type.";
    }

    /**
     * 自定义的监控属性的配置文件位置
     *
     * @return
     */
    @Override
    public String getMetricsConfPath() {
        return AgentConfiguration.INSTANCE.getZkMetricsConfPath();
    }

    /**
     * JMX连接的服务名
     *
     * @return
     */
    @Override
    public String getServerName() {
        return AgentConfiguration.INSTANCE.getZkJmxServerName();
    }
}
