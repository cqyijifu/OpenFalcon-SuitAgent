/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.jmx.JMXConnection;
import com.yiji.falcon.agent.jmx.vo.JMXConnectionInfo;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.jmx.vo.JMXObjectNameInfo;
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.plugins.util.CacheUtil;
import com.yiji.falcon.agent.util.MapUtil;
import com.yiji.falcon.agent.util.Maths;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.jmx.JMXMetricsConfiguration;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.CompositeDataSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * JMX监控值
 *
 * @author guqiu@yiji.com
 */
public class JMXMetricsValue extends MetricsCommon {
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private JMXPlugin jmxPlugin;
    private List<JMXMetricsValueInfo> jmxMetricsValueInfos;
    private final static ConcurrentHashMap<String,String> serverDirNameCatch = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,String> serverDirPathCatch = new ConcurrentHashMap<>();

    /**
     * JMX监控值
     *
     * @param jmxPlugin
     * @param jmxMetricsValueInfos
     */
    public JMXMetricsValue(JMXPlugin jmxPlugin, List<JMXMetricsValueInfo> jmxMetricsValueInfos) {
        this.jmxPlugin = jmxPlugin;
        this.jmxMetricsValueInfos = jmxMetricsValueInfos;
    }

    /**
     * 获取配置文件配置的监控值
     *
     * @return
     */
    private Set<JMXMetricsConfiguration> getMetricsConfig() {
        Set<JMXMetricsConfiguration> jmxMetricsConfigurations = new HashSet<>();

        setMetricsConfig("agent.common.metrics.type.", AgentConfiguration.INSTANCE.getJmxCommonMetricsConfPath(), jmxMetricsConfigurations);
        setMetricsConfig(jmxPlugin.basePropertiesKey(),
                AgentConfiguration.INSTANCE.getPluginConfPath() + File.separator + jmxPlugin.configFileName(), jmxMetricsConfigurations);

        return jmxMetricsConfigurations;
    }

    /**
     * 设置配置的jmx监控属性
     *
     * @param basePropertiesKey        配置属性的前缀key值
     * @param propertiesPath           监控属性的配置文件路径
     * @param jmxMetricsConfigurations 需要保存的集合对象
     * @throws IOException
     */
    private void setMetricsConfig(String basePropertiesKey, String propertiesPath, Set<JMXMetricsConfiguration> jmxMetricsConfigurations) {

        if (!StringUtils.isEmpty(basePropertiesKey) &&
                !StringUtils.isEmpty(propertiesPath)) {
            try (FileInputStream in = new FileInputStream(propertiesPath)) {
                Properties properties = new Properties();
                properties.load(in);
                for (int i = 1; i <= 100; i++) {
                    String objectName = basePropertiesKey + i + ".objectName";
                    if (!StringUtils.isEmpty(properties.getProperty(objectName))) {
                        JMXMetricsConfiguration metricsConfiguration = new JMXMetricsConfiguration();
                        metricsConfiguration.setObjectName(properties.getProperty(objectName));//设置ObjectName
                        metricsConfiguration.setCounterType(properties.getProperty(basePropertiesKey + i + ".counterType"));//设置counterType
                        metricsConfiguration.setMetrics(properties.getProperty(basePropertiesKey + i + ".metrics"));//设置metrics
                        metricsConfiguration.setValueExpress(properties.getProperty(basePropertiesKey + i + ".valueExpress"));//设置metrics
                        String tag = properties.getProperty(basePropertiesKey + i + ".tag");
                        metricsConfiguration.setTag(StringUtils.isEmpty(tag) ? "" : tag);//设置tag
                        String alias = properties.getProperty(basePropertiesKey + i + ".alias");
                        metricsConfiguration.setAlias(StringUtils.isEmpty(alias) ? metricsConfiguration.getMetrics() : alias);

                        jmxMetricsConfigurations.add(metricsConfiguration);
                    }
                }
            } catch (IOException e) {
                log.error("配置文件读取失败", e);
            }
        }
    }

    /**
     * 构建监控值报告的中间对象
     */
    private class KitObjectNameMetrics {
        JMXObjectNameInfo jmxObjectNameInfo;
        JMXMetricsConfiguration jmxMetricsConfiguration;
    }

    /**
     * 获取监控值报告的中间对象的辅助方法
     *
     * @param jmxObjectNameInfos
     * @param metricsConfiguration
     * @return
     */
    private Set<KitObjectNameMetrics> getKitObjectNameMetrics(Collection<JMXObjectNameInfo> jmxObjectNameInfos, JMXMetricsConfiguration metricsConfiguration) {
        Set<KitObjectNameMetrics> kitObjectNameMetricsSet = new HashSet<>();
        for (JMXObjectNameInfo jmxObjectNameInfo : jmxObjectNameInfos) {
            String objectName = jmxObjectNameInfo.getObjectName().toString();
            Map<String, String> metricsMap = jmxObjectNameInfo.getMetricsValue();
            if (objectName.contains(metricsConfiguration.getObjectName())) {
                if (metricsMap.get(metricsConfiguration.getMetrics()) != null ||
                        metricsMap.get(metricsConfiguration.getAlias()) != null) {
                    KitObjectNameMetrics kitObjectNameMetrics = new KitObjectNameMetrics();
                    kitObjectNameMetrics.jmxObjectNameInfo = jmxObjectNameInfo;
                    kitObjectNameMetrics.jmxMetricsConfiguration = metricsConfiguration;
                    kitObjectNameMetricsSet.add(kitObjectNameMetrics);
                }
            }
        }
        return kitObjectNameMetricsSet;
    }

    /**
     * 生成监控报告的辅助方法
     *
     * @param kitObjectNameMetricses
     * @param metricsValueInfo
     * @return
     */
    private Set<FalconReportObject> generatorReportObject(Collection<KitObjectNameMetrics> kitObjectNameMetricses, JMXMetricsValueInfo metricsValueInfo) {
        Set<FalconReportObject> result = new HashSet<>();

        //用于判断监控值是否重复添加,若出现重复添加,进行监控值比较
        Map<String, FalconReportObject> repeat = new HashMap<>();

        for (KitObjectNameMetrics kitObjectNameMetrics : kitObjectNameMetricses) {
            JMXObjectNameInfo jmxObjectNameInfo = kitObjectNameMetrics.jmxObjectNameInfo;
            JMXMetricsConfiguration jmxMetricsConfiguration = kitObjectNameMetrics.jmxMetricsConfiguration;
            String metricsValue = jmxObjectNameInfo.getMetricsValue().get(jmxMetricsConfiguration.getMetrics());
            if (metricsValue != null) {
                //服务的标识后缀名
                String name = metricsValueInfo.getJmxConnectionInfo().getName();

                FalconReportObject requestObject = new FalconReportObject();
                setReportCommonValue(requestObject, jmxPlugin.step());
                requestObject.setMetric(getMetricsName(jmxMetricsConfiguration.getAlias()));//设置push obj 的 metrics
                try {
                    //设置push obj 的 Counter
                    requestObject.setCounterType(CounterType.valueOf(jmxMetricsConfiguration.getCounterType()));
                } catch (IllegalArgumentException e) {
                    log.error("错误的{} counterType配置:{},只能是 {} 或 {},未修正前,将忽略此监控值", jmxMetricsConfiguration.getAlias(), jmxMetricsConfiguration.getCounterType(), CounterType.COUNTER, CounterType.GAUGE, e);
                    continue;
                }
                requestObject.setTimestamp(System.currentTimeMillis() / 1000);
                requestObject.setObjectName(jmxObjectNameInfo.getObjectName());
                Object newValue = executeJsExpress(kitObjectNameMetrics.jmxMetricsConfiguration.getValueExpress(), metricsValue);
                if (NumberUtils.isNumber(String.valueOf(newValue).trim())) {
                    requestObject.setValue(String.valueOf(newValue).trim());
                } else {
                    log.error("异常:监控指标值{} - {} : {}不能转换为数字,将忽略此监控值", jmxMetricsConfiguration.getObjectName(), jmxMetricsConfiguration.getMetrics(), metricsValue);
                    continue;
                }

                requestObject.appendTags(getTags(name, jmxPlugin, jmxPlugin.serverName(), MetricsType.JMX_OBJECT_CONF)).appendTags(jmxMetricsConfiguration.getTag());
                String dirName = getServerDirName(metricsValueInfo.getJmxConnectionInfo().getPid(),
                        metricsValueInfo.getJmxConnectionInfo().getConnectionServerName());
                if (!StringUtils.isEmpty(dirName)) {
                    requestObject.appendTags("dir=" + dirName);
                }

                //监控值重复性判断
                FalconReportObject reportInRepeat = repeat.get(jmxMetricsConfiguration.getMetrics());
                if (reportInRepeat == null) {
                    //第一次添加
                    result.add(requestObject);
                    repeat.put(jmxMetricsConfiguration.getMetrics(), requestObject);
                } else {
                    if (!reportInRepeat.equals(requestObject)) {
                        // 若已有记录而且不相同,进行区分保存
                        result.remove(reportInRepeat);
                        reportInRepeat.appendTags(requestObject.getObjectName().toString());//JMX 的ObjectName名称符合tag格式
                        result.add(reportInRepeat);

                        requestObject.appendTags(requestObject.getObjectName().toString());
                        if (!result.contains(requestObject)) {
                            result.add(requestObject);
                        }
                    }
                }
            }
        }
        return result;
    }


    private String getServerDirPath(int pid,String serverName){
        String key = serverName + pid;
        String serverDirPath = CacheUtil.getCacheValue(serverDirPathCatch.get(key));
        if(serverDirPath == null){
            serverDirPath = jmxPlugin.serverPath(pid,serverName);
            if(!StringUtils.isEmpty(serverDirPath)){
                serverDirPathCatch.put(key,CacheUtil.setCacheValue(serverDirPath));
            }
        }
        return serverDirPath;
    }

    private String getServerDirName(int pid,String serverName){
        String key = serverName + pid;
        String dirName = CacheUtil.getCacheValue(serverDirNameCatch.get(key));
        if(dirName == null){
            dirName = jmxPlugin.serverDirName(pid);
            if(!StringUtils.isEmpty(dirName)){
                serverDirNameCatch.put(key,CacheUtil.setCacheValue(dirName));
            }
        }
        return dirName;
    }

    /**
     * 获取所有的监控值报告
     *
     * @return
     * @throws IOException
     */
    @Override
    public Collection<FalconReportObject> getReportObjects() {
        Set<FalconReportObject> result = new HashSet<>();

        //清除过期的缓存
        CacheUtil.getTimeoutCacheKeys(serverDirNameCatch).forEach(serverDirNameCatch::remove);
        CacheUtil.getTimeoutCacheKeys(serverDirPathCatch).forEach(serverDirPathCatch::remove);

        for (JMXMetricsValueInfo metricsValueInfo : jmxMetricsValueInfos) {

            //JMX 服务是否已被停掉的检查
            JMXConnectionInfo jmxConnectionInfo = metricsValueInfo.getJmxConnectionInfo();
            String key = jmxConnectionInfo.getConnectionServerName() + jmxConnectionInfo.getPid();
            if (jmxConnectionInfo.getPid() != 0 && jmxConnectionInfo.getConnectionServerName() != null) {
                String serverDirPath = getServerDirPath(jmxConnectionInfo.getPid(),jmxConnectionInfo.getConnectionServerName());
                if (!StringUtils.isEmpty(serverDirPath)) {
                    if (serverDirPath.contains(" ")) {
                        log.warn("发现路径: {} 有空格,请及时处理,否则Agent可能会工作不正常", serverDirPath);
                    }
                    if (!jmxConnectionInfo.isValid()) {
                        File file = new File(serverDirPath);
                        if (!file.exists()) {
                            //JMX服务目录不存在,清除连接,跳过此次监控
                            JMXConnection.removeConnectCache(jmxConnectionInfo.getConnectionServerName(), jmxConnectionInfo.getPid());
                            try {
                                jmxConnectionInfo.getJmxConnector().close();
                            } catch (Exception ignored) {
                            }

                            //清理缓存数据
                            for (Object k : MapUtil.getSameValueKeys(serverDirPathCatch, serverDirPathCatch.get(key))) {
                                serverDirPathCatch.remove(String.valueOf(k));
                            }
                            for (Object k : MapUtil.getSameValueKeys(serverDirNameCatch, serverDirNameCatch.get(key))) {
                                serverDirNameCatch.remove(String.valueOf(k));
                            }
                            continue;
                        }
                    }
                }
            }

            if(jmxConnectionInfo.getmBeanServerConnection() != null
                    && jmxConnectionInfo.getCacheKeyId() != null
                    && jmxConnectionInfo.getConnectionQualifiedServerName() != null){
                String dirName = getServerDirName(jmxConnectionInfo.getPid(),jmxConnectionInfo.getConnectionServerName());
                if (!jmxConnectionInfo.isValid()) {
                    //该连接不可用,添加该 jmx不可用的监控报告
                    FalconReportObject reportObject = generatorVariabilityReport(false, jmxConnectionInfo.getName(), jmxPlugin.step(), jmxPlugin, jmxPlugin.serverName());
                    if (jmxConnectionInfo.getPid() != 0) {
                        if (!StringUtils.isEmpty(dirName)) {
                            reportObject.appendTags("dir=" + dirName);
                        }
                    }
                    result.add(reportObject);
                } else {
                    Set<KitObjectNameMetrics> kitObjectNameMetricsSet = new HashSet<>();

                    for (JMXMetricsConfiguration metricsConfiguration : getMetricsConfig()) {// 配置文件配置的需要监控的
                        kitObjectNameMetricsSet.addAll(getKitObjectNameMetrics(metricsValueInfo.getJmxObjectNameInfoList(), metricsConfiguration));
                    }

                    result.addAll(generatorReportObject(kitObjectNameMetricsSet, metricsValueInfo));

                    //添加可用性报告
                    result.add(generatorVariabilityReport(true, jmxConnectionInfo.getName(), jmxPlugin.step(), jmxPlugin, jmxPlugin.serverName()));

                    //添加內建报告
                    result.addAll(getInbuiltReportObjects(metricsValueInfo));
                    Collection<FalconReportObject> inbuilt = jmxPlugin.inbuiltReportObjectsForValid(metricsValueInfo);
                    if (inbuilt != null && !inbuilt.isEmpty()) {
                        for (FalconReportObject reportObject : inbuilt) {
                            if (!StringUtils.isEmpty(dirName)) {
                                reportObject.appendTags("dir=" + dirName);
                            }
                            result.add(reportObject);
                        }
                    }
                }
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
    private Collection<FalconReportObject> getInbuiltReportObjects(JMXMetricsValueInfo metricsValueInfo) {
        List<FalconReportObject> result = new ArrayList<>();
        if (metricsValueInfo == null || !metricsValueInfo.getJmxConnectionInfo().isValid()) {
            return result;
        }
        try {
            for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
                //服务的标识后缀名
                String name = objectNameInfo.getJmxConnectionInfo().getName();

                FalconReportObject falconReportObject = new FalconReportObject();
                setReportCommonValue(falconReportObject, jmxPlugin.step());
                falconReportObject.setCounterType(CounterType.GAUGE);
                falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
                falconReportObject.setObjectName(objectNameInfo.getObjectName());
                falconReportObject.appendTags(getTags(name, jmxPlugin, jmxPlugin.serverName(), MetricsType.JMX_OBJECT_IN_BUILD));
                String dirName = getServerDirName(metricsValueInfo.getJmxConnectionInfo().getPid(),
                        metricsValueInfo.getJmxConnectionInfo().getConnectionServerName());
                if (!StringUtils.isEmpty(dirName)) {
                    falconReportObject.appendTags("dir=" + dirName);
                }

                if ("java.lang:type=Memory".equals(objectNameInfo.getObjectName().toString())) {

                    MemoryUsage heapMemoryUsage = MemoryUsage.from((CompositeDataSupport) objectNameInfo.
                            getJmxConnectionInfo().getmBeanServerConnection().getAttribute(objectNameInfo.getObjectName(), "HeapMemoryUsage"));
                    MemoryUsage nonHeapMemoryUsage = MemoryUsage.from((CompositeDataSupport) objectNameInfo.
                            getJmxConnectionInfo().getmBeanServerConnection().getAttribute(objectNameInfo.getObjectName(), "NonHeapMemoryUsage"));

                    falconReportObject.setMetric(getMetricsName("HeapMemoryCommitted"));
                    falconReportObject.setValue(String.valueOf(heapMemoryUsage.getCommitted()));
                    result.add(falconReportObject.clone());

                    falconReportObject.setMetric(getMetricsName("NonHeapMemoryCommitted"));
                    falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getCommitted()));
                    result.add(falconReportObject.clone());

                    falconReportObject.setMetric(getMetricsName("HeapMemoryFree"));
                    falconReportObject.setValue(String.valueOf(heapMemoryUsage.getMax() - heapMemoryUsage.getUsed()));
                    result.add(falconReportObject.clone());

                    falconReportObject.setMetric(getMetricsName("HeapMemoryMax"));
                    falconReportObject.setValue(String.valueOf(heapMemoryUsage.getMax()));
                    result.add(falconReportObject.clone());

                    falconReportObject.setMetric(getMetricsName("HeapMemoryUsed"));
                    falconReportObject.setValue(String.valueOf(heapMemoryUsage.getUsed()));
                    result.add(falconReportObject.clone());

                    falconReportObject.setMetric(getMetricsName("NonHeapMemoryUsed"));
                    falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getUsed()));
                    result.add(falconReportObject.clone());

                    //堆内存使用比例
                    falconReportObject.setMetric(getMetricsName("HeapMemoryUsedRatio"));
                    falconReportObject.setValue(String.valueOf(Maths.div(heapMemoryUsage.getUsed(), heapMemoryUsage.getMax(), 2) * 100));
                    result.add(falconReportObject.clone());

                    if (nonHeapMemoryUsage.getMax() == -1) {
                        falconReportObject.setMetric(getMetricsName("NonHeapMemoryUsedRatio"));
                        falconReportObject.setValue("-1");
                        result.add(falconReportObject.clone());
                    } else {
                        falconReportObject.setMetric(getMetricsName("NonHeapMemoryUsedRatio"));
                        falconReportObject.setValue(String.valueOf(Maths.div(nonHeapMemoryUsage.getUsed(), nonHeapMemoryUsage.getMax(), 2) * 100));
                        result.add(falconReportObject.clone());

                        falconReportObject.setMetric(getMetricsName("NonHeapMemoryMax"));
                        falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getMax()));
                        result.add(falconReportObject.clone());

                        falconReportObject.setMetric(getMetricsName("NonHeapMemoryFree"));
                        falconReportObject.setValue(String.valueOf(nonHeapMemoryUsage.getMax() - nonHeapMemoryUsage.getUsed()));
                        result.add(falconReportObject.clone());
                    }

                }else if("java.lang:type=MemoryPool,name=Metaspace".equals(objectNameInfo.getObjectName().toString())){
                    MemoryUsage metaspaceUsage = MemoryUsage.from((CompositeDataSupport) objectNameInfo.
                            getJmxConnectionInfo().getmBeanServerConnection().getAttribute(objectNameInfo.getObjectName(), "Usage"));

                    falconReportObject.setMetric(getMetricsName("MetaspaceMemoryCommitted"));
                    falconReportObject.setValue(String.valueOf(metaspaceUsage.getCommitted()));
                    result.add(falconReportObject.clone());

                    falconReportObject.setMetric(getMetricsName("MetaspaceMemoryUsed"));
                    falconReportObject.setValue(String.valueOf(metaspaceUsage.getUsed()));
                    result.add(falconReportObject.clone());

                    if (metaspaceUsage.getMax() == -1) {
                        falconReportObject.setMetric(getMetricsName("MetaspaceMemoryUsedRatio"));
                        falconReportObject.setValue("-1");
                        result.add(falconReportObject.clone());
                    } else {
                        falconReportObject.setMetric(getMetricsName("MetaspaceMemoryUsedRatio"));
                        falconReportObject.setValue(String.valueOf(Maths.div(metaspaceUsage.getUsed(), metaspaceUsage.getMax(), 2) * 100));
                        result.add(falconReportObject.clone());

                        falconReportObject.setMetric(getMetricsName("MetaspaceMemoryMax"));
                        falconReportObject.setValue(String.valueOf(metaspaceUsage.getMax()));
                        result.add(falconReportObject.clone());

                        falconReportObject.setMetric(getMetricsName("MetaspaceMemoryFree"));
                        falconReportObject.setValue(String.valueOf(metaspaceUsage.getMax() - metaspaceUsage.getUsed()));
                        result.add(falconReportObject.clone());
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取jmx 内置监控数据异常", e);
        }
        return result;
    }

}
