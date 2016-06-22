/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * metrics监控公共抽象类
 * @author guqiu@yiji.com
 */
public abstract class MetricsCommon {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        setReportCommonValue(falconReportObject);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric(getMetricsName("availability"));
        falconReportObject.setValue(isAva ? "1" : "0");
        falconReportObject.appendTags(getTags(name,MetricsType.AVAILABILITY));
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        return falconReportObject;
    }

    /**
     * 返回进行变量转换后的endPoint
     * @param endPoint
     * @return
     */
    protected String getEndpointByTrans(String endPoint){
        String hostIP = "unKnowHostIP";
        String hostName = "unKnowHostName";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostIP = addr.getHostAddress();
            hostName = addr.getHostName();
        } catch (UnknownHostException e) {
            logger.error("获取系统Host信息失败",e);
        }
        return endPoint.replace("{host.ip}",hostIP).
                replace("{host.name}",hostName);
    }

    /**
     * 执行js表达式并返回执行后的结果
     * @param express
     * 表达式
     * @param value
     * 原值
     * @return
     * 返回新值或返回原值(执行失败时)
     */
    protected Object executeJsExpress(String express, Object value){
        Object newValue = value;
        if(!StringUtils.isEmpty(express)){
            try {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("javascript");
                engine.put("value", value);
                engine.put("newValue", "");
                engine.getBindings(ScriptContext.ENGINE_SCOPE);
                engine.eval(express);
                newValue = engine.get("newValue");
            } catch (ScriptException e) {
                logger.error("执行js表达式错误",e);
            }
        }

        return newValue;
    }

    /**
     * 获取Agent计算后的服务标识tag
     * 将自动打上service service.type agentSignName,metrics.type标签
     * @param name
     * 服务标识
     * @return
     */
    public String getTags(String name,MetricsType metricsType){
        String signName = "service=" + getType();
        if(this.getClass().getSuperclass() == JMXMetricsValue.class){
            signName += ",service.type=jmx";
        }else if(this.getClass().getSuperclass() == JDBCMetricsValue.class){
            signName += ",service.type=database";
        }else{
            signName += ",service.type=unKnow";
        }
        if(metricsType != null){
            signName += ",metrics.type=" + metricsType.getTypeName();
        }
        if(StringUtils.isEmpty(name)){
            return signName;
        }
        return signName + ",agentSignName=" + name;
    }

    /**
     * 获取metrics名称(进行服务区分后的名称)
     * @param metricsName
     * metrics 名字
     * @return
     */
    protected String getMetricsName(String metricsName) {
        return metricsName;
    }

    /**
     * 监控类型
     * @return
     */
    public abstract String getType();

    /**
     * 设置报告对象公共的属性
     * @param falconReportObject
     */
    public abstract void setReportCommonValue(FalconReportObject falconReportObject);

}
