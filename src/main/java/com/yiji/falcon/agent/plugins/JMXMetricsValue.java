package com.yiji.falcon.agent.plugins;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/3.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.jmx.vo.JMXObjectNameInfo;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.JMXMetricsConfiguration;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 从JMX获取监控值抽象类
 * Created by QianLong on 16/5/3.
 */
public abstract class JMXMetricsValue {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    protected List<JMXMetricsValueInfo> metricsValueInfos;

    /**
     * 获取所有的具体服务的JMX监控值VO
     * @return
     */
    protected abstract List<JMXMetricsValueInfo> getMetricsValueInfos();

    public JMXMetricsValue() {
        this.metricsValueInfos = getMetricsValueInfos();
    }

    /**
     * 获取配置文件配置的监控值
     * @return
     */
    private List<JMXMetricsConfiguration> getMetricsConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(AgentConfiguration.INSTANCE.getAgentConfPath()));

        List<JMXMetricsConfiguration> metricsConfigurationList = new ArrayList<>();
        String basePropertiesKey = getBasePropertiesKey();
        for (int i = 1; i <= 100; i++) {
            String objectName = basePropertiesKey + i +".objectName";
            if(!StringUtils.isEmpty(properties.getProperty(objectName))){
                JMXMetricsConfiguration metricsConfiguration = new JMXMetricsConfiguration();
                metricsConfiguration.setObjectName(properties.getProperty(objectName));//设置ObjectName
                metricsConfiguration.setCounterType(properties.getProperty(basePropertiesKey + i + ".counterType"));//设置counterType
                metricsConfiguration.setMetrics(properties.getProperty(basePropertiesKey + i + ".metrics"));//设置metrics
                String tag = properties.getProperty(basePropertiesKey + i + ".tag");
                metricsConfiguration.setTag(StringUtils.isEmpty(tag) ? "" : tag);//设置tag
                String alias = properties.getProperty(basePropertiesKey + i + ".alias");
                metricsConfiguration.setAlias(StringUtils.isEmpty(alias) ? metricsConfiguration.getMetrics() : alias);

                metricsConfigurationList.add(metricsConfiguration);
            }
        }
        return metricsConfigurationList;
    }

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     * @return
     */
    protected abstract Collection<FalconReportObject> getInbuiltReportObjectsForValid();

    /**
     * 获取所有的监控值报告
     * @return
     * @throws IOException
     */
    public Collection<FalconReportObject> getReportObjects() throws IOException {
        Set<FalconReportObject> result = new HashSet<>();

        if(metricsValueInfos == null || metricsValueInfos.isEmpty()){
            //获取不到监控值,返回所有zk不可用的监控报告
            log.error(getType() + " JMX 连接获取失败");
            result.add(generatorVariabilityReport(false,"allUnVariability"));
            return result;
        }
        for (JMXMetricsValueInfo metricsValueInfo : metricsValueInfos) {

            /**
             * 用于判断监控值是否重复添加,若出现重复添加,进行监控值比较
             */
            Map<String,FalconReportObject> repeat = new HashMap<>();

            if(!metricsValueInfo.getJmxConnectionInfo().isValid()){
                //该连接不可用,添加该zk jmx不可用的监控报告
                result.add(generatorVariabilityReport(false,metricsValueInfo.getJmxConnectionInfo().getName()));
            }else{

                Set<JMXObjectNameInfo> validJmxObjectInfos = new HashSet<>();

                for (JMXMetricsConfiguration metricsConfiguration : getMetricsConfig()) {// 配置文件配置的需要监控的
                    // 如果ObjectName匹配
                    validJmxObjectInfos.addAll(metricsValueInfo.getJmxObjectNameInfoList().stream().filter(objectNameInfo -> objectNameInfo.getObjectName().toString().contains(metricsConfiguration.getObjectName())).collect(Collectors.toList()));
                }

                for (JMXMetricsConfiguration metricsConfiguration : getMetricsConfig()) {// 配置文件配置的需要监控的
                    for (JMXObjectNameInfo objectNameInfo : validJmxObjectInfos) {
                        Map<String,String> objectNameAllValue = objectNameInfo.getMetricsValue();

                        if(objectNameAllValue.get(metricsConfiguration.getMetrics()) != null){//若ObjectName 的 监控值中有配置项指定的监控值
                            FalconReportObject requestObject = new FalconReportObject();
                            setReportCommonValue(requestObject,metricsValueInfo.getJmxConnectionInfo().getName());
                            requestObject.setMetric(metricsConfiguration.getAlias());//设置push obj 的 metrics
                            try {
                                //设置push obj 的 Counter
                                requestObject.setCounterType(CounterType.valueOf(metricsConfiguration.getCounterType()));
                            } catch (IllegalArgumentException e) {
                                log.error("错误的{} counterType配置:{},只能是 {} 或 {},未修正前,将忽略此监控值",metricsConfiguration.getAlias(),metricsConfiguration.getCounterType(),CounterType.COUNTER,CounterType.GAUGE);
                                continue;
                            }
                            requestObject.setTags(metricsConfiguration.getTag());
                            requestObject.setTimestamp(System.currentTimeMillis() / 1000);
                            requestObject.setObjectName(objectNameInfo.getObjectName());
                            if(NumberUtils.isNumber(objectNameAllValue.get(metricsConfiguration.getMetrics()))){
                                requestObject.setValue(objectNameAllValue.get(metricsConfiguration.getMetrics()));
                            }else{
                                log.error("异常:监控指标值{} - {} : {}不能转换为数字,将忽略此监控值",metricsConfiguration.getObjectName(),metricsConfiguration.getMetrics(),objectNameAllValue.get(metricsConfiguration.getMetrics()));
                                continue;
                            }

                            //监控值重复性判断
                            FalconReportObject reportInRepeat = repeat.get(metricsConfiguration.getMetrics());
                            if(reportInRepeat == null){
                                //第一次添加
                                result.add(requestObject);
                                repeat.put(metricsConfiguration.getMetrics(),requestObject);
                            }else{
                                if(!reportInRepeat.equals(requestObject)){
                                    // 若已有记录而且不相同,进行区分保存
                                    result.remove(reportInRepeat);
                                    reportInRepeat.setMetric(metricsConfiguration.getMetrics() + "(" + reportInRepeat.getObjectName().toString().replace("\"","") + ")");
                                    result.add(reportInRepeat);

                                    requestObject.setMetric(metricsConfiguration.getMetrics() + "(" + requestObject.getObjectName().toString().replace("\"","") + ")");
                                    if(!result.contains(requestObject)){
                                        result.add(requestObject);
                                    }
                                }
                            }
                        }
                    }

                }
                //添加可用性报告
                result.add(generatorVariabilityReport(true,metricsValueInfo.getJmxConnectionInfo().getName()));
            }

            //添加內建报告
            Collection<FalconReportObject> inbuilt = getInbuiltReportObjectsForValid();
            if(inbuilt != null && !inbuilt.isEmpty()){
                result.addAll(inbuilt);
            }

        }

        return result;
    }

    /**
     * 创建指定可用性的报告对象
     * @param isAva
     * @param name 报告对象的连接标识名
     * @return
     */
    private FalconReportObject generatorVariabilityReport(boolean isAva, String name){
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject,name);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric("availability");
        falconReportObject.setValue(isAva ? "1" : "0");
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
    protected void setReportCommonValue(FalconReportObject falconReportObject,String name){
        if(falconReportObject != null){
            falconReportObject.setEndpoint(AgentConfiguration.INSTANCE.getAgentEndpoint() + "-" + getType() + (StringUtils.isEmpty(name) ? "" : ":" + name));
            falconReportObject.setStep(getStep());
        }
    }

    /**
     * 获取step
     * @return
     */
    public abstract int getStep();

    /**
     * 监控类型
     * @return
     */
    public abstract String getType();

    /**
     * 监控值配置项基础配置名
     * @return
     */
    public abstract String getBasePropertiesKey();

    /**
     * JMX连接的服务名
     * @return
     */
    public abstract String getServerName();
}
