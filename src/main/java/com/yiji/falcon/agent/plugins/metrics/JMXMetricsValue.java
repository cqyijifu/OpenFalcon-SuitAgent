/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;

import com.yiji.falcon.agent.exception.JMXUnavailabilityType;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;

import javax.management.openmbean.CompositeDataSupport;
import java.io.File;
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
@Slf4j
public class JMXMetricsValue extends MetricsCommon {

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
        if(jmxObjectNameInfos != null){
            for (JMXObjectNameInfo jmxObjectNameInfo : jmxObjectNameInfos) {
                String objectName = jmxObjectNameInfo.getObjectName().toString();
                Map<String, Object> metricsMap = jmxObjectNameInfo.getMetricsValue();
                if (objectName.contains(metricsConfiguration.getObjectName())) {
                    if (metricsMap.get(metricsConfiguration.getMetrics()) != null ||
                            metricsMap.get(metricsConfiguration.getAlias()) != null) {
                        metricsConfiguration.setHasCollect(true);
                        KitObjectNameMetrics kitObjectNameMetrics = new KitObjectNameMetrics();
                        kitObjectNameMetrics.jmxObjectNameInfo = jmxObjectNameInfo;
                        kitObjectNameMetrics.jmxMetricsConfiguration = metricsConfiguration;
                        kitObjectNameMetricsSet.add(kitObjectNameMetrics);
                    }
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
            Object metricsValue = jmxObjectNameInfo.getMetricsValue().get(jmxMetricsConfiguration.getMetrics());
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
                requestObject.setTimestamp(metricsValueInfo.getTimestamp());
                requestObject.setObjectName(jmxObjectNameInfo.getObjectName());
                Object newValue = executeJsExpress(kitObjectNameMetrics.jmxMetricsConfiguration.getValueExpress(), metricsValue.toString());
                if (NumberUtils.isNumber(String.valueOf(newValue).trim())) {
                    requestObject.setValue(String.valueOf(newValue).trim());
                } else {
                    log.error("异常:监控指标值{} - {} : {}不能转换为数字,将忽略此监控值", jmxMetricsConfiguration.getObjectName(), jmxMetricsConfiguration.getMetrics(), metricsValue);
                    continue;
                }

                requestObject.appendTags(getTags(name, jmxPlugin, jmxPlugin.serverName(), MetricsType.JMX_OBJECT_CONF)).appendTags(jmxMetricsConfiguration.getTag());

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
     * 清除JMX连接
     * @param jmxConnectionInfo
     */
    private void removeJMXConnectCache(JMXConnectionInfo jmxConnectionInfo){
        String key = jmxConnectionInfo.getConnectionServerName() + jmxConnectionInfo.getPid();
        JMXConnection.removeConnectCache(jmxConnectionInfo.getConnectionServerName(), jmxConnectionInfo.getPid());
        jmxConnectionInfo.closeJMXConnector();

        //清理缓存数据
        for (Object k : MapUtil.getSameValueKeys(serverDirPathCatch, serverDirPathCatch.get(key))) {
            serverDirPathCatch.remove(String.valueOf(k));
        }
        for (Object k : MapUtil.getSameValueKeys(serverDirNameCatch, serverDirNameCatch.get(key))) {
            serverDirNameCatch.remove(String.valueOf(k));
        }
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

        //JMX连接清除检查处理
        for (JMXMetricsValueInfo metricsValueInfo : jmxMetricsValueInfos) {
            JMXConnectionInfo jmxConnectionInfo = metricsValueInfo.getJmxConnectionInfo();
            if(StringUtils.isEmpty(jmxConnectionInfo.getName())){
                //清除没有agentSignName的JMX连接
                removeJMXConnectCache(jmxConnectionInfo);
                continue;
            }
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
                            removeJMXConnectCache(jmxConnectionInfo);
                            continue;
                        }
                    }
                }
            }

            if(!jmxConnectionInfo.isValid()){
                //添加该 jmx不可用的监控报告
                if(jmxConnectionInfo.getType() == null){
                    result.add(generatorVariabilityReport(false,jmxConnectionInfo.getName(),metricsValueInfo.getTimestamp(), jmxPlugin.step(), jmxPlugin, jmxPlugin.serverName()));
                }else {
                    result.add(generatorVariabilityReport(false,String.valueOf(jmxConnectionInfo.getType().getType()),metricsValueInfo.getTimestamp(), jmxConnectionInfo.getName(), jmxPlugin.step(), jmxPlugin, jmxPlugin.serverName()));
                }
            }else {
                //添加可用性报告
                result.add(generatorVariabilityReport(true, jmxConnectionInfo.getName(),metricsValueInfo.getTimestamp(), jmxPlugin.step(), jmxPlugin, jmxPlugin.serverName()));
            }

            if(jmxConnectionInfo.getmBeanServerConnection() != null
                    && jmxConnectionInfo.getCacheKeyId() != null
                    && jmxConnectionInfo.getConnectionQualifiedServerName() != null){
                String dirName = getServerDirName(jmxConnectionInfo.getPid(),jmxConnectionInfo.getConnectionServerName());
                if (hasContinueReport(jmxConnectionInfo)) {
                    Set<KitObjectNameMetrics> kitObjectNameMetricsSet = new HashSet<>();
                    Set<JMXMetricsConfiguration> jmxMetricsConfigurationSet = metricsValueInfo.getJmxMetricsConfigurations();
                    for (JMXMetricsConfiguration metricsConfiguration : jmxMetricsConfigurationSet) {// 配置文件配置的需要监控的
                        kitObjectNameMetricsSet.addAll(getKitObjectNameMetrics(metricsValueInfo.getJmxObjectNameInfoList(), metricsConfiguration));
                    }

                    //输出未采集到的指标
                    final List<String> noCollects = new ArrayList<>();
                    final List<String> hasCollects = new ArrayList<>();
                    jmxMetricsConfigurationSet.stream().filter(conf -> !conf.isHasCollect()).
                            forEach(conf -> noCollects.add(String.format("【metrics：%s，alias：%s】",conf.getMetrics(),conf.getAlias())));
                    jmxMetricsConfigurationSet.stream().filter(JMXMetricsConfiguration::isHasCollect).
                            forEach(conf -> hasCollects.add(String.format("【metrics：%s，alias：%s】",conf.getMetrics(),conf.getAlias())));
                    if(!noCollects.isEmpty()){
                        log.warn("当前未采集到的指标({})：{}",noCollects.size(),noCollects);
                    }
                    log.warn("当前已采集到的指标({})：{}",hasCollects.size(),hasCollects);

                    result.addAll(generatorReportObject(kitObjectNameMetricsSet, metricsValueInfo));

                    //添加內建报告
                    result.addAll(getInbuiltReportObjects(metricsValueInfo));

                    //添加插件內建报告
                    Collection<FalconReportObject> inbuilt = jmxPlugin.inbuiltReportObjectsForValid(metricsValueInfo);
                    result.addAll(inbuilt);

                    result.stream().filter(reportObject -> !StringUtils.isEmpty(dirName)).forEach(reportObject -> {
                        reportObject.appendTags("dir=" + dirName);
                    });
                }
            }

        }

        return result;
    }

    /**
     * 判断是否继续进行JMX报告获取
     * @param jmxConnectionInfo
     * @return
     */
    private boolean hasContinueReport(JMXConnectionInfo jmxConnectionInfo){
        JMXUnavailabilityType type = jmxConnectionInfo.getType();
        if(jmxConnectionInfo.isValid()){
            //可用，返回true
            return true;
        }else if(!jmxConnectionInfo.isValid() && type != null && type != JMXUnavailabilityType.connectionFailed){
            //不可用，但是并不是连接失败，返回true，上报已经采集到的值
            return true;
        }
        return false;
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
        if (metricsValueInfo == null) {
            return result;
        }
        if(!metricsValueInfo.getJmxConnectionInfo().isValid() && metricsValueInfo.getJmxConnectionInfo().getType() == JMXUnavailabilityType.connectionFailed){
            return result;
        }
        boolean hasHeapCollect = false;
        boolean hasMetaCollect = false;
        try {
            for (JMXObjectNameInfo objectNameInfo : metricsValueInfo.getJmxObjectNameInfoList()) {
                //服务的标识后缀名
                String name = objectNameInfo.getJmxConnectionInfo().getName();

                FalconReportObject falconReportObject = new FalconReportObject();
                setReportCommonValue(falconReportObject, jmxPlugin.step());
                falconReportObject.setCounterType(CounterType.GAUGE);
                falconReportObject.setTimestamp(metricsValueInfo.getTimestamp());
                falconReportObject.setObjectName(objectNameInfo.getObjectName());
                falconReportObject.appendTags(getTags(name, jmxPlugin, jmxPlugin.serverName(), MetricsType.JMX_OBJECT_IN_BUILD));

                if ("java.lang:type=Memory".equals(objectNameInfo.getObjectName().toString())) {
                    hasHeapCollect = true;
                    MemoryUsage heapMemoryUsage = MemoryUsage.from((CompositeDataSupport) objectNameInfo.getMetricsValue().get("HeapMemoryUsage"));
                    MemoryUsage nonHeapMemoryUsage = MemoryUsage.from((CompositeDataSupport) objectNameInfo.getMetricsValue().get("NonHeapMemoryUsage"));

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
                    hasMetaCollect = true;
                    MemoryUsage metaspaceUsage = MemoryUsage.from((CompositeDataSupport) objectNameInfo.getMetricsValue().get("Usage"));

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

            if (!hasHeapCollect){
                log.warn("此次可能因监控值超时原因，堆内存的相关信息未能采集到。");
            }
            if(!hasMetaCollect){
                log.warn("JDK版本小于8或可能因监控值采集超时原因，元空间内存的相关信息未能采集到");
            }

        } catch (Exception e) {
            log.error("获取 {} jmx 内置监控数据异常:{}",metricsValueInfo.getJmxConnectionInfo().getName() ,e.getMessage());
        }
        return result;
    }

}
