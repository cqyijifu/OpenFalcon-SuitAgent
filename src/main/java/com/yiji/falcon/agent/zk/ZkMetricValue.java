package com.yiji.falcon.agent.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.jmx.vo.JMXObjectNameInfo;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by QianLong on 16/4/25.
 */
public class ZkMetricValue {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static String serverName = AgentConfiguration.INSTANCE.getZK_JMX_SERVER_NAME();

    /**
     * 获取配置文件指定的指标参数报告对象
     * @return
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     * @throws IOException
     */
    public List<FalconReportObject> getConfReportObjects() throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(AgentConfiguration.INSTANCE.getAGENT_CONF_PATH()));

        Map<String,Set<String>> confMetrics = new HashMap<>();
        String basePropertiesKey = "agent.zk.metrics.type.";
        for (int i = 1; i <= 100; i++) {
            String key = basePropertiesKey + i;
            String value = properties.getProperty(key);
            if(!StringUtils.isEmpty(value)){
                String[] ss = value.split(":");
                if(ss.length < 3){
                    log.warn("无效的配置参数:{}",value);
                    continue;
                }
                if(confMetrics.get(ss[0]) == null){
                    Set<String> valueSet = new HashSet<>();
                    if(ss.length == 4){
                        valueSet.add(ss[1] + ":" + ss[2] + ":" + ss[3]);
                    }else{
                        valueSet.add(ss[1] + ":" + ss[2]);
                    }
                    confMetrics.put(ss[0],valueSet);
                }else{
                    Set<String> valueSet = confMetrics.get(ss[0]);
                    if(ss.length == 4){
                        valueSet.add(ss[1] + ":" + ss[2] + ":" + ss[3]);
                    }else{
                        valueSet.add(ss[1] + ":" + ss[2]);
                    }
                    confMetrics.put(ss[0],valueSet);
                }
            }
        }


        List<FalconReportObject> result = new ArrayList<>();
        List<JMXMetricsValueInfo> metricsValueInfos = JMXManager.getJmxMetricValue(serverName,new ZKJMXConnection());
        if(metricsValueInfos == null || metricsValueInfos.size() == 0){
            //获取不到监控值,返回所有zk不可用的监控报告
            log.error("zookeeper JMX 连接获取失败");
            result.add(generatorVariabilityReport(false,"all"));
            return result;
        }
        for (JMXMetricsValueInfo metricsValueInfo : metricsValueInfos) {

            boolean isLeader = false;

            if(!metricsValueInfo.getJmxConnectionInfo().isValid()){
                //该连接不可用,添加该zk jmx不可用的监控报告
                result.add(generatorVariabilityReport(false,metricsValueInfo.getJmxConnectionInfo().getName()));
            }else{
                for (String confNeedsObjectName : confMetrics.keySet()) {// 配置文件配置的需要监控的ObjectName
                    for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
                        if(objectNameInfo.toString().contains("Leader")){
                            //若ObjectName中包含有 Leader 则该zk为Leader角色
                            isLeader = true;
                        }
                        if(objectNameInfo.getObjectName().toString().contains(confNeedsObjectName)){// 如果ObjectName匹配
                            Map<String,String> objectNameAllValue = objectNameInfo.getMetricsValue();
                            Set<String> valueSet = confMetrics.get(confNeedsObjectName);
                            for (String confNeedsMetrics : valueSet) {// 配置文件配置的需要获取的监控值
                                String metrics = "";
                                String counterType = "";
                                String tag = "";
                                String[] conf = confNeedsMetrics.split(":");
                                metrics = conf[0];
                                counterType = conf[1];
                                if(conf.length == 3){
                                    tag = conf[2];
                                }
                                if(objectNameAllValue.get(metrics) != null){//若ObjectName 的 监控值中有配置项指定的监控值
                                    FalconReportObject requestObject = new FalconReportObject();
                                    setReportCommonValue(requestObject,metricsValueInfo.getJmxConnectionInfo().getName());
                                    requestObject.setMetric(metrics);//设置push obj 的 metrics
                                    try {
                                        //设置push obj 的 Counter
                                        requestObject.setCounterType(CounterType.valueOf(counterType));
                                    } catch (IllegalArgumentException e) {
                                        log.error("错误的counterType配置:{},只能是 {} 或 {}",conf[1],CounterType.COUNTER,CounterType.GAUGE);
                                    }
                                    requestObject.setTags(tag);
                                    requestObject.setTimestamp(System.currentTimeMillis() / 1000);
                                    try {
                                        requestObject.setValue(Double.parseDouble(objectNameAllValue.get(metrics)));// 指定监控值的具体数值
                                    } catch (NumberFormatException e) {
                                        log.error("异常:监控指标值{} - {} : {}不能转换为数字",confNeedsObjectName,confNeedsMetrics,objectNameAllValue.get(metrics));
                                    }

                                    result.add(requestObject);
                                }
                            }
                        }
                    }
                }
            }

            //添加isLeader报告
            result.add(generatorIsLeaderReport(isLeader,metricsValueInfo.getJmxConnectionInfo().getName()));

        }

        return result;
    }

    /**
     * 创建指定可用性的报告对象
     * @param isAva
     * @param name 报告对象的连接标识名
     * @return
     */
    private FalconReportObject generatorVariabilityReport(boolean isAva,String name){
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject,name);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric("availability-zookeeper");
        falconReportObject.setValue(isAva ? 1 : 0);
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        return falconReportObject;
    }

    /**
     * 创建指定可用性的报告对象
     * @param isLeader
     * @param name 报告对象的连接标识名
     * @return
     */
    private FalconReportObject generatorIsLeaderReport(boolean isLeader,String name){
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject,name);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric("isLeader");
        falconReportObject.setValue(isLeader ? 1 : 0);
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        return falconReportObject;
    }

    /**
     * 设置报告对象公共的属性
     * endpoint
     * step
     * @param falconReportObject
     * @param name 报告对象的连接标识名
     */
    private void setReportCommonValue(FalconReportObject falconReportObject,String name){
        if(falconReportObject != null){
            falconReportObject.setEndpoint(AgentConfiguration.INSTANCE.getAGENT_ENDPOINT() + "-zookeeper" + (StringUtils.isEmpty(name) ? "" : ":" + name));
            falconReportObject.setStep(AgentConfiguration.INSTANCE.getZK_STEP());
        }
    }

}
