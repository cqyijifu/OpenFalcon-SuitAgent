/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 10:56 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
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
        if(addressCollection != null && !addressCollection.isEmpty()){
            addressCollection.forEach(address -> {
                DetectResult detectResult = detectPlugin.detectResult(address);
                if(detectResult != null){
                    //可用性
                    if(detectResult.isSuccess()){
                        FalconReportObject falconReportObject = MetricsCommon.generatorVariabilityReport(true,detectPlugin.agentSignName(address),detectPlugin.step(),detectPlugin,detectPlugin.serverName());
                        addTagFromDetectResult(detectResult,falconReportObject);
                        result.add(falconReportObject);
                    }else{
                        FalconReportObject falconReportObject = MetricsCommon.generatorVariabilityReport(false,detectPlugin.agentSignName(address),detectPlugin.step(),detectPlugin,detectPlugin.serverName());
                        addTagFromDetectResult(detectResult,falconReportObject);
                        result.add(falconReportObject);
                    }
                    //自定义Metrics
                    Map<String,Double> metricsMap = detectResult.getMetricsMap();
                    if(metricsMap != null && !metricsMap.isEmpty()){
                        metricsMap.forEach((metrics,value) -> {
                            FalconReportObject reportObject = new FalconReportObject();
                            reportObject.setMetric(MetricsCommon.getMetricsName(metrics));
                            reportObject.setCounterType(CounterType.GAUGE);
                            reportObject.setValue(value + "");
                            reportObject.setTimestamp(System.currentTimeMillis() / 1000);
                            reportObject.appendTags(MetricsCommon.getTags(detectPlugin.agentSignName(address),detectPlugin,detectPlugin.serverName(), MetricsType.SQL_CONF));
                            MetricsCommon.setReportCommonValue(reportObject,detectPlugin.step());
                            addTagFromDetectResult(detectResult,reportObject);
                            result.add(reportObject);
                        });
                    }
                }
            });
        }
        return result;
    }

    private void addTagFromDetectResult(DetectResult detectResult, FalconReportObject reportObject){
       String tag = detectResult.getTag();
        if(!StringUtils.isEmpty(tag)){
            reportObject.appendTags(tag);
        }
    }
}
