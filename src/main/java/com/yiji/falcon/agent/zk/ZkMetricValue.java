package com.yiji.falcon.agent.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.mchange.v2.util.DoubleWeakHashMap;
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

        List<MetricsConfiguration> metricsConfigurationList = new ArrayList<>();
        String basePropertiesKey = "agent.zk.metrics.type.";
        for (int i = 1; i <= 100; i++) {
            String objectName = basePropertiesKey + i +".objectName";
            if(!StringUtils.isEmpty(properties.getProperty(objectName))){
                MetricsConfiguration metricsConfiguration = new MetricsConfiguration();
                metricsConfiguration.setObjectName(properties.getProperty(objectName));//设置ObjectName
                metricsConfiguration.setCounterType(properties.getProperty(basePropertiesKey + i + ".counterType"));//设置counterType
                metricsConfiguration.setMetrics(properties.getProperty(basePropertiesKey + i + ".metrics"));//设置metrics
                String tag = properties.getProperty(basePropertiesKey + i + ".tag");
                metricsConfiguration.setTag(StringUtils.isEmpty(tag) ? "" : tag);//设置tag

                metricsConfigurationList.add(metricsConfiguration);
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

            /**
             * 用于判断监控值是否重复添加,若出现重复添加,进行监控值比较
             */
            Map<String,FalconReportObject> repeat = new HashMap<>();

            if(!metricsValueInfo.getJmxConnectionInfo().isValid()){
                //该连接不可用,添加该zk jmx不可用的监控报告
                result.add(generatorVariabilityReport(false,metricsValueInfo.getJmxConnectionInfo().getName()));
            }else{
                for (MetricsConfiguration metricsConfiguration : metricsConfigurationList) {// 配置文件配置的需要监控的
                    for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
                        if(objectNameInfo.toString().contains("Leader")){
                            //若ObjectName中包含有 Leader 则该zk为Leader角色
                            isLeader = true;
                        }
                        if(objectNameInfo.getObjectName().toString().contains(metricsConfiguration.getObjectName())){// 如果ObjectName匹配
                            Map<String,String> objectNameAllValue = objectNameInfo.getMetricsValue();

                            if(objectNameAllValue.get(metricsConfiguration.getMetrics()) != null){//若ObjectName 的 监控值中有配置项指定的监控值
                                FalconReportObject requestObject = new FalconReportObject();
                                setReportCommonValue(requestObject,metricsValueInfo.getJmxConnectionInfo().getName());
                                requestObject.setMetric(metricsConfiguration.getMetrics());//设置push obj 的 metrics
                                try {
                                    //设置push obj 的 Counter
                                    requestObject.setCounterType(CounterType.valueOf(metricsConfiguration.getCounterType()));
                                } catch (IllegalArgumentException e) {
                                    log.error("错误的counterType配置:{},只能是 {} 或 {}",metricsConfiguration.getCounterType(),CounterType.COUNTER,CounterType.GAUGE);
                                }
                                requestObject.setTags(metricsConfiguration.getTag());
                                requestObject.setTimestamp(System.currentTimeMillis() / 1000);
                                try {
                                    requestObject.setValue(Double.parseDouble(objectNameAllValue.get(metricsConfiguration.getMetrics())));// 指定监控值的具体数值
                                } catch (NumberFormatException e) {
                                    log.error("异常:监控指标值{} - {} : {}不能转换为数字",metricsConfiguration.getObjectName(),metricsConfiguration.getMetrics(),objectNameAllValue.get(metricsConfiguration.getMetrics()));
                                }

                                //监控值重复性判断
                                FalconReportObject reportInRepeat = repeat.get(metricsConfiguration.getMetrics());
                                if(reportInRepeat == null){
                                    //第一次添加
                                    result.add(requestObject);
                                    repeat.put(metricsConfiguration.getMetrics(),requestObject);
                                }else{
                                    if(reportInRepeat.getValue() == 0 && requestObject.getValue() != 0){
                                        //替换有值的
                                        result.remove(reportInRepeat);
                                        result.add(requestObject);
                                        repeat.put(metricsConfiguration.getMetrics(),requestObject);
                                    }else if(reportInRepeat.getValue() != 0 && requestObject.getValue() != 0 && reportInRepeat.getValue() != requestObject.getValue()){
                                        //都有值,而且不同,保存两者
                                        result.add(requestObject);
                                        repeat.put(metricsConfiguration.getMetrics(),requestObject);
                                    }
                                }
                            }
                        }
                    }
                }
                //添加可用性报告
                result.add(generatorVariabilityReport(true,metricsValueInfo.getJmxConnectionInfo().getName()));
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
