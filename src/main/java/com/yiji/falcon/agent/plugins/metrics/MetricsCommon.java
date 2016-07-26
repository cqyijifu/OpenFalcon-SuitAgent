/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.*;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * metrics监控公共类
 * @author guqiu@yiji.com
 */
public abstract class MetricsCommon {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCommon.class);

    /**
     * 获取所有的监控值报告
     * @return
     */
    abstract public Collection<FalconReportObject> getReportObjects();

    /**
     * 创建指定可用性的报告对象
     * @param isAva
     * @param agentSignName
     * @param step
     * @param plugin
     * @param serverName
     * @return
     */
    public static FalconReportObject generatorVariabilityReport(boolean isAva, String agentSignName, int step, Plugin plugin, String serverName){
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject,step);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric(getMetricsName("availability"));
        falconReportObject.setValue(isAva ? "1" : "0");
        falconReportObject.appendTags(getTags(agentSignName,plugin,serverName,MetricsType.AVAILABILITY));
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        return falconReportObject;
    }

    /**
     * 返回进行变量转换后的endPoint
     * @param endPoint
     * @return
     */
    public static String getEndpointByTrans(String endPoint){
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
                replace("{host.name}",hostName).trim();
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
    public static Object executeJsExpress(String express, Object value){
        Object newValue = value;
        if(!StringUtils.isEmpty(express)){
            try {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("nashorn");
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
     * @param agentSignName
     * @param plugin
     * @param serverName
     * @param metricsType
     * @return
     */
    public static String getTags(String agentSignName,Plugin plugin,String serverName,MetricsType metricsType){
        String signName = "service=" + serverName;
        if(JMXPlugin.class.isAssignableFrom(plugin.getClass())){
            signName += ",service.type=jmx";
        }else if(JDBCPlugin.class.isAssignableFrom(plugin.getClass())){
            signName += ",service.type=database";
        }else if(SNMPV3Plugin.class.isAssignableFrom(plugin.getClass())){
            signName += ",service.type=snmp";
        }else if(DetectPlugin.class.isAssignableFrom(plugin.getClass())){
            signName += ",service.type=detect";
        }else{
            signName += ",service.type=unKnow";
        }
        if(metricsType != null){
            signName += ",metrics.type=" + metricsType.getTypeName();
        }
        if(StringUtils.isEmpty(agentSignName)){
            return signName;
        }
        return signName + ",agentSignName=" + agentSignName;
    }

    /**
     * 获取metrics名称(进行服务区分后的名称)
     * @param metricsName
     * metrics 名字
     * @return
     */
    public static String getMetricsName(String metricsName) {
        return metricsName;
    }

    /**
     * 设置FalconReportObject共有的属性值
     * @param falconReportObject
     * @param step
     */
    public static void setReportCommonValue(FalconReportObject falconReportObject,int step){
        if(falconReportObject != null){
            falconReportObject.setEndpoint(getEndpointByTrans(AgentConfiguration.INSTANCE.getAgentEndpoint()));
            falconReportObject.setStep(step);
        }
    }
}
