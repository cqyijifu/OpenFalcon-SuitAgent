package com.yiji.falcon.agent.plugins.tomcat;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/3.
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

import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by QianLong on 16/5/3.
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
     *
     * @return
     */
    @Override
    protected Collection<FalconReportObject> getInbuiltReportObjectsForValid() {
        List<FalconReportObject> result = new ArrayList<>();
        try {
            for (JMXMetricsValueInfo metricsValueInfo : metricsValueInfos) {
                for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
                    if("java.lang:type=Memory".equals(objectNameInfo.getObjectName().toString())){
                        MemoryUsage heapMemoryUsage =  MemoryUsage.from((CompositeDataSupport)objectNameInfo.
                                getJmxConnectionInfo().getmBeanServerConnection().getAttribute(objectNameInfo.getObjectName(), "HeapMemoryUsage"));
                        FalconReportObject falconReportObject = new FalconReportObject();
                        setReportCommonValue(falconReportObject,objectNameInfo.getJmxConnectionInfo().getName());
                        falconReportObject.setCounterType(CounterType.GAUGE);
                        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
                        falconReportObject.setObjectName(objectNameInfo.getObjectName());

                        falconReportObject.setMetric("HeapMemoryCommitted");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getCommitted()));
                        result.add(falconReportObject);

                        falconReportObject.setMetric("HeapMemoryFree");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getMax() - heapMemoryUsage.getUsed()));
                        result.add(falconReportObject);

                        falconReportObject.setMetric("HeapMemoryMax");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getMax()));
                        result.add(falconReportObject);

                        falconReportObject.setMetric("HeapMemoryUsed");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getUsed()));
                        result.add(falconReportObject);

                    }
                }
            }
        } catch (Exception e) {
            log.error("获取tomcat agent内置监控数据异常",e);
        }
        return result;
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
     * 监控值配置项基础配置名
     *
     * @return
     */
    @Override
    public String getBasePropertiesKey() {
        return "agent.tomcat.metrics.type.";
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
