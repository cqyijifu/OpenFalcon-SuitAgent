package com.yiji.falcon.agent.plugins;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/25.
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.util.StringUtils;

/**
 * Created by QianLong on 16/5/25.
 */
public abstract class MetricsCommon {

    /**
     * 创建指定可用性的报告对象
     * @param isAva
     * 是否可用
     * @param name
     * 服务的标识后缀名
     * @return
     */
    FalconReportObject generatorVariabilityReport(boolean isAva, String name){
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject,name);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric(getMetricsName("availability",name));
        falconReportObject.setValue(isAva ? "1" : "0");
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        return falconReportObject;
    }

    /**
     * 获取metrics名称(进行服务区分后的名称)
     * @param metricsName
     * metrics 名字
     * @param name
     * 服务的标识后缀名
     * @return
     */
    public String getMetricsName(String metricsName,String name) {
        return getType() + "." + metricsName + (StringUtils.isEmpty(name) ? "" : "/" + name);
    }

    /**
     * 监控类型
     * @return
     */
    public abstract String getType();

    /**
     * 设置报告对象公共的属性
     * @param falconReportObject
     * @param name
     */
    public abstract void setReportCommonValue(FalconReportObject falconReportObject,String name);

}
