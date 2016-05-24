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

import javax.management.openmbean.CompositeDataSupport;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.*;

/**
 * 从JMX获取监控值抽象类
 * Created by QianLong on 16/5/3.
 */
public abstract class JMXMetricsValue {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    protected List<JMXMetricsValueInfo> metricsValueInfos;

    public JMXMetricsValue() {
        this.metricsValueInfos = getMetricsValueInfos();
    }

    /**
     * 获取所有的具体服务的JMX监控值VO
     * @return
     */
    protected abstract List<JMXMetricsValueInfo> getMetricsValueInfos();

    /**
     * 获取配置文件配置的监控值
     * @return
     */
    private Set<JMXMetricsConfiguration> getMetricsConfig() throws IOException {
        Set<JMXMetricsConfiguration> jmxMetricsConfigurations = new HashSet<>();

        setMetricsConfig("agent.common.metrics.type.",AgentConfiguration.INSTANCE.getJmxCommonMetricsConfPath(),jmxMetricsConfigurations);
        setMetricsConfig(getBasePropertiesKey(),getMetricsConfPath(),jmxMetricsConfigurations);

        return jmxMetricsConfigurations;
    }

    /**
     * 设置配置的jmx监控属性
     * @param basePropertiesKey
     * 配置属性的前缀key值
     * @param propertiesPath
     * 监控属性的配置文件路径
     * @param jmxMetricsConfigurations
     * 需要保存的集合对象
     * @throws IOException
     */
    private void setMetricsConfig(String basePropertiesKey,String propertiesPath,Set<JMXMetricsConfiguration> jmxMetricsConfigurations) throws IOException {
        if(!StringUtils.isEmpty(basePropertiesKey) &&
                !StringUtils.isEmpty(propertiesPath)){
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesPath));
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

                    jmxMetricsConfigurations.add(metricsConfiguration);
                }
            }
        }
    }

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     * @return
     */
    protected abstract Collection<FalconReportObject> getInbuiltReportObjectsForValid();

    /**
     * 构建监控值报告的中间对象
     */
    private class KitObjectNameMetrics {
        JMXObjectNameInfo jmxObjectNameInfo;
        JMXMetricsConfiguration jmxMetricsConfiguration;
    }

    /**
     * 获取监控值报告的中间对象的辅助方法
     * @param jmxObjectNameInfos
     * @param metricsConfiguration
     * @return
     */
    private Set<KitObjectNameMetrics> getKitObjectNameMetrics(Collection<JMXObjectNameInfo> jmxObjectNameInfos, JMXMetricsConfiguration metricsConfiguration){
        Set<KitObjectNameMetrics> kitObjectNameMetricsSet = new HashSet<>();
        for (JMXObjectNameInfo jmxObjectNameInfo : jmxObjectNameInfos) {
            if(jmxObjectNameInfo.getObjectName().toString().contains(metricsConfiguration.getObjectName())){
                KitObjectNameMetrics kitObjectNameMetrics = new KitObjectNameMetrics();
                kitObjectNameMetrics.jmxObjectNameInfo = jmxObjectNameInfo;
                kitObjectNameMetrics.jmxMetricsConfiguration = metricsConfiguration;
                kitObjectNameMetricsSet.add(kitObjectNameMetrics);
            }
        }
        return kitObjectNameMetricsSet;
    }

    /**
     * 生成监控报告的辅助方法
     * @param kitObjectNameMetricses
     * @param metricsValueInfo
     * @return
     */
    private Set<FalconReportObject> generatorReportObject(Collection<KitObjectNameMetrics> kitObjectNameMetricses,JMXMetricsValueInfo metricsValueInfo){
        Set<FalconReportObject> result = new HashSet<>();
        /**
         * 用于判断监控值是否重复添加,若出现重复添加,进行监控值比较
         */
        Map<String,FalconReportObject> repeat = new HashMap<>();

        for (KitObjectNameMetrics kitObjectNameMetrics : kitObjectNameMetricses) {
            JMXObjectNameInfo jmxObjectNameInfo = kitObjectNameMetrics.jmxObjectNameInfo;
            JMXMetricsConfiguration jmxMetricsConfiguration = kitObjectNameMetrics.jmxMetricsConfiguration;
            String metricsValue = jmxObjectNameInfo.getMetricsValue().get(jmxMetricsConfiguration.getMetrics());
            if(metricsValue != null){
                FalconReportObject requestObject = new FalconReportObject();
                setReportCommonValue(requestObject,metricsValueInfo.getJmxConnectionInfo().getName());
                requestObject.setMetric(jmxMetricsConfiguration.getAlias());//设置push obj 的 metrics
                try {
                    //设置push obj 的 Counter
                    requestObject.setCounterType(CounterType.valueOf(jmxMetricsConfiguration.getCounterType()));
                } catch (IllegalArgumentException e) {
                    log.error("错误的{} counterType配置:{},只能是 {} 或 {},未修正前,将忽略此监控值",jmxMetricsConfiguration.getAlias(),jmxMetricsConfiguration.getCounterType(),CounterType.COUNTER,CounterType.GAUGE,e);
                    continue;
                }
                requestObject.setTags(jmxMetricsConfiguration.getTag());
                requestObject.setTimestamp(System.currentTimeMillis() / 1000);
                requestObject.setObjectName(jmxObjectNameInfo.getObjectName());
                if(NumberUtils.isNumber(metricsValue)){
                    requestObject.setValue(metricsValue);
                }else{
                    log.error("异常:监控指标值{} - {} : {}不能转换为数字,将忽略此监控值",jmxMetricsConfiguration.getObjectName(),jmxMetricsConfiguration.getMetrics(),metricsValue);
                    continue;
                }

                //监控值重复性判断
                FalconReportObject reportInRepeat = repeat.get(jmxMetricsConfiguration.getMetrics());
                if(reportInRepeat == null){
                    //第一次添加
                    result.add(requestObject);
                    repeat.put(jmxMetricsConfiguration.getMetrics(),requestObject);
                }else{
                    if(!reportInRepeat.equals(requestObject)){
                        // 若已有记录而且不相同,进行区分保存
                        result.remove(reportInRepeat);
                        reportInRepeat.setMetric(jmxMetricsConfiguration.getMetrics() + "(" + reportInRepeat.getObjectName().toString().replace("\"","") + ")");
                        result.add(reportInRepeat);

                        requestObject.setMetric(jmxMetricsConfiguration.getMetrics() + "(" + requestObject.getObjectName().toString().replace("\"","") + ")");
                        if(!result.contains(requestObject)){
                            result.add(requestObject);
                        }
                    }
                }
            }
        }
        return result;
    }

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

            if(!metricsValueInfo.getJmxConnectionInfo().isValid()){
                //该连接不可用,添加该 jmx不可用的监控报告
                result.add(generatorVariabilityReport(false,metricsValueInfo.getJmxConnectionInfo().getName()));
            }else{

                Set<KitObjectNameMetrics> kitObjectNameMetricsSet = new HashSet<>();

                for (JMXMetricsConfiguration metricsConfiguration : getMetricsConfig()) {// 配置文件配置的需要监控的
                    kitObjectNameMetricsSet.addAll(getKitObjectNameMetrics(metricsValueInfo.getJmxObjectNameInfoList(),metricsConfiguration));
                }

                result.addAll(generatorReportObject(kitObjectNameMetricsSet,metricsValueInfo));

                //添加可用性报告
                result.add(generatorVariabilityReport(true,metricsValueInfo.getJmxConnectionInfo().getName()));
            }

            //添加內建报告
            result.addAll(getInbuiltReportObjects());
            Collection<FalconReportObject> inbuilt = getInbuiltReportObjectsForValid();
            if(inbuilt != null && !inbuilt.isEmpty()){
                result.addAll(inbuilt);
            }

        }

        return result;
    }

    /**
     * 內建监控报告
     * HeapMemoryCommitted
     * NonHeapMemoryCommitted
     * HeapMemoryFree
     * NonHeapMemoryFree
     * HeapMemoryMax
     * NonHeapMemoryMax
     * HeapMemoryUsed
     * NonHeapMemoryUsed
     *
     * @return
     */
    private Collection<FalconReportObject> getInbuiltReportObjects() {
        List<FalconReportObject> result = new ArrayList<>();
        try {
            for (JMXMetricsValueInfo metricsValueInfo : metricsValueInfos) {
                for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
                    if("java.lang:type=Memory".equals(objectNameInfo.getObjectName().toString())){
                        MemoryUsage heapMemoryUsage =  MemoryUsage.from((CompositeDataSupport)objectNameInfo.
                                getJmxConnectionInfo().getmBeanServerConnection().getAttribute(objectNameInfo.getObjectName(), "HeapMemoryUsage"));
                        MemoryUsage nonHeapMemoryUsage =  MemoryUsage.from((CompositeDataSupport)objectNameInfo.
                                getJmxConnectionInfo().getmBeanServerConnection().getAttribute(objectNameInfo.getObjectName(), "NonHeapMemoryUsage"));
                        FalconReportObject falconReportObject = new FalconReportObject();
                        setReportCommonValue(falconReportObject,objectNameInfo.getJmxConnectionInfo().getName());
                        falconReportObject.setCounterType(CounterType.GAUGE);
                        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
                        falconReportObject.setObjectName(objectNameInfo.getObjectName());

                        falconReportObject.setMetric("HeapMemoryCommitted");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getCommitted()));
                        result.add(falconReportObject);
                        falconReportObject.setMetric("NonHeapMemoryCommitted");
                        falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getCommitted()));
                        result.add(falconReportObject);

                        falconReportObject.setMetric("HeapMemoryFree");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getMax() - heapMemoryUsage.getUsed()));
                        result.add(falconReportObject);
                        falconReportObject.setMetric("NonHeapMemoryFree");
                        falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getMax() - nonHeapMemoryUsage.getUsed()));
                        result.add(falconReportObject);

                        falconReportObject.setMetric("HeapMemoryMax");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getMax()));
                        result.add(falconReportObject);
                        falconReportObject.setMetric("NonHeapMemoryMax");
                        falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getMax()));
                        result.add(falconReportObject);

                        falconReportObject.setMetric("HeapMemoryUsed");
                        falconReportObject.setValue(String.valueOf(heapMemoryUsage.getUsed()));
                        result.add(falconReportObject);
                        falconReportObject.setMetric("NonHeapMemoryUsed");
                        falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getUsed()));
                        result.add(falconReportObject);

                    }
                }
            }
        } catch (Exception e) {
            log.error("获取jmx 内置监控数据异常",e);
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
     * 监控属性的配置文件位置
     * @return
     */
    public abstract String getMetricsConfPath();

    /**
     * JMX连接的服务名
     * @return
     */
    public abstract String getServerName();
}
