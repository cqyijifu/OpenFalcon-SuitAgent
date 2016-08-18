/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 10:56 创建
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author guqiu@yiji.com
 */
public class DetectMetricsValue extends MetricsCommon {

    private DetectPlugin detectPlugin;

    public DetectMetricsValue(DetectPlugin detectPlugin) {
        this.detectPlugin = detectPlugin;
    }

    /**
     * 获取所有的监控值报告
     *
     * @return
     */
    @Override
    public Collection<FalconReportObject> getReportObjects() {
        Set<FalconReportObject> result = new HashSet<>();
        Collection<String> addressCollection = detectPlugin.detectAddressCollection();
        if(addressCollection == null || addressCollection.isEmpty()){
            //若无配置地址,获取自动探测的地址
            addressCollection = detectPlugin.autoDetectAddress();
        }
        if(addressCollection != null && !addressCollection.isEmpty()){
            addressCollection.forEach(address -> {
                DetectResult detectResult = detectPlugin.detectResult(address);
                if(detectResult != null){
                    //可用性
                    if(detectResult.isSuccess()){
                        FalconReportObject falconReportObject = MetricsCommon.generatorVariabilityReport(true,detectPlugin.agentSignName(address),detectPlugin.step(),detectPlugin,detectPlugin.serverName());
                        addCommonTagFromDetectResult(detectResult,falconReportObject);
                        result.add(falconReportObject);
                    }else{
                        FalconReportObject falconReportObject = MetricsCommon.generatorVariabilityReport(false,detectPlugin.agentSignName(address),detectPlugin.step(),detectPlugin,detectPlugin.serverName());
                        addCommonTagFromDetectResult(detectResult,falconReportObject);
                        result.add(falconReportObject);
                    }
                    //自定义Metrics
                    List<DetectResult.Metric> metricsList = detectResult.getMetricsList();
                    if(metricsList != null && !metricsList.isEmpty()){
                        metricsList.forEach(metric -> {
                            FalconReportObject reportObject = new FalconReportObject();
                            reportObject.setMetric(MetricsCommon.getMetricsName(metric.metricName));
                            reportObject.setCounterType(metric.counterType);
                            reportObject.setValue(metric.value);
                            reportObject.setTimestamp(System.currentTimeMillis() / 1000);
                            //打默认tag
                            reportObject.appendTags(MetricsCommon.getTags(detectPlugin.agentSignName(address),detectPlugin,detectPlugin.serverName(), MetricsType.SQL_CONF))
                                    //打该监控值指定的tag
                                    .appendTags(metric.tags);
                            MetricsCommon.setReportCommonValue(reportObject,detectPlugin.step());
                            addCommonTagFromDetectResult(detectResult,reportObject);
                            result.add(reportObject);
                        });
                    }
                }
            });
        }
        return result;
    }

    /**
     * 设置探测结果的公共的tag
     * @param detectResult
     * @param reportObject
     */
    private void addCommonTagFromDetectResult(DetectResult detectResult, FalconReportObject reportObject){
       String tag = detectResult.getCommonTag();
        if(!StringUtils.isEmpty(tag)){
            reportObject.appendTags(tag);
        }
    }
}
