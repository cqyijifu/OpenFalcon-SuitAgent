/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;

import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.*;
import com.yiji.falcon.agent.util.DateUtil;
import com.yiji.falcon.agent.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * metrics监控公共类
 * @author guqiu@yiji.com
 */
@Slf4j
public abstract class MetricsCommon {

    private static final ConcurrentHashMap<String,Long> mockValid = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,Set<String>> mockService = new ConcurrentHashMap<>();


    private static String getMockValidKey(String serviceType,String service){
        return serviceType + "-" + service;
    }

    private static boolean isExistFromMockValid(String serviceType, String service){
        String key = getMockValidKey(serviceType,service);
        return mockValid.containsKey(key);
    }

    /**
     * 添加Mock的有效时间记录
     * @param serviceType
     * @param service
     */
    private static void addMockValidShutdownTime(String serviceType, String service){
        String key = getMockValidKey(serviceType,service);
        if(!isExistFromMockValid(serviceType,service)){
            log.info("添加mock服务的停机时间 - {}:{}",serviceType,service);
            mockValid.put(key,System.currentTimeMillis());
        }
    }

    /**
     * 删除Mock有效时间记录
     * @param serviceType
     * @param service
     */
    private static void removeMockValidTimeRecord(String serviceType,String service){
        String key = getMockValidKey(serviceType,service);
        if(mockValid.remove(key) != null){
            log.info("已移除 Mock有效性时间记录 - {}:{}",serviceType,service);
        }
    }

    /**
     * 检查Mock是否有效
     * @param serviceType
     * @param service
     * @return
     * 0 : mock有效
     * >0 : mock无效,返回的数值代表当前服务已停机时间
     */
    private static long isValidMock(String serviceType, String service){
        String key = getMockValidKey(serviceType,service);
        Long shutdownTime = mockValid.get(key);
        if(shutdownTime == null){
            // 未获取到记录,说明目标服务还未添加停机时间,返回可用
            return 0;
        }
        int maxTime = AgentConfiguration.INSTANCE.getMockValidTime() * 1000;

        long timeout = System.currentTimeMillis() - shutdownTime;

        if(timeout <= maxTime){
            return 0;
        }else{
            return timeout;
        }
    }

    /**
     * 获取当前mock的服务列表
     * @return
     * JSON数据
     */
    public static String getMockServicesList(){
        JSONObject result = new JSONObject();

        for (String key : mockService.keySet()) {
            JSONObject jsonObject = new JSONObject();
            Set<String> services = mockService.get(key);
            for (String service : services) {
                jsonObject.put("service",service);
                long shutdownTime = isValidMock(key,service);
                jsonObject.put("isTimeout",shutdownTime > 0);
                jsonObject.put("shutdownTime",shutdownTime);
            }
            result.put(key,jsonObject);
        }

        return result.toJSONString();
    }

    /**
     * 添加mock服务
     * @param serviceType
     * @param service
     */
    public static void addMockService(String serviceType,String service){

        if(mockService.get(serviceType) == null){
            Set<String> mockServices = new HashSet<>();
            mockServices.add(service);
            mockService.put(serviceType,mockServices);
        }else{
            Set<String> mockServices = mockService.get(serviceType);
            mockServices.add(service);
            mockService.put(serviceType,mockServices);
        }
        log.info("已添加Mock服务 - {}:{}",serviceType,service);
    }

    /**
     * 删除mock服务
     * @param serviceType
     * @param service
     */
    public static void removeMockService(String serviceType,String service){
        if(mockService.get(serviceType) != null){
            Set<String> mockServices = mockService.get(serviceType);
            if(mockServices.remove(service)){
                log.info("已移除Mock服务 - {}:{}",serviceType,service);
            }
            mockService.put(serviceType,mockServices);
            removeMockValidTimeRecord(serviceType,service);
        }
    }

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
        return generatorVariabilityReport(isAva,isAva?"1":"0",System.currentTimeMillis() / 1000,agentSignName,step,plugin,serverName);
    }

    /**
     * 创建指定可用性的报告对象
     * @param isAva
     * @param agentSignName
     * @param step
     * @param plugin
     * @param serverName
     * @return
     */
    public static FalconReportObject generatorVariabilityReport(boolean isAva, String agentSignName,long timestamp, int step, Plugin plugin, String serverName){
        return generatorVariabilityReport(isAva,isAva?"1":"0",timestamp,agentSignName,step,plugin,serverName);
    }

    /**
     * 创建指定可用性的报告对象
     * @param isAva
     * @param avaValue
     * 指定可用性的值
     * @param agentSignName
     * @param step
     * @param plugin
     * @param serverName
     * @return
     */
    public static FalconReportObject generatorVariabilityReport(boolean isAva,String avaValue,long timestamp, String agentSignName, int step, Plugin plugin, String serverName){
        log.info("Availability Generator : {}-{}-{}-{}",isAva,avaValue,serverName,agentSignName);
        FalconReportObject falconReportObject = new FalconReportObject();
        setReportCommonValue(falconReportObject,step);
        falconReportObject.setCounterType(CounterType.GAUGE);
        falconReportObject.setMetric(getMetricsName("availability"));
        falconReportObject.setValue(avaValue);
        falconReportObject.appendTags(getTags(agentSignName,plugin,serverName,MetricsType.AVAILABILITY));
        falconReportObject.setTimestamp(timestamp);

        if(!isAva){
            //mock处理
            boolean isOK = false;
            for (String key : mockService.keySet()) {
                String targetType = "service.type=" + key;
                String tags = falconReportObject.getTags();
                if(!StringUtils.isEmpty(tags)){
                    if(tags.contains(targetType)){
                        Set<String> mockServices = mockService.get(key);
                        for (String targetService : mockServices) {
                            //判断Mock有效性
                            long mockTime = isValidMock(key,targetService);
                            if(mockTime > 0){
                                log.info("发现超时的mock服务 - {}:{} 已超时: {} 毫秒",key,targetService,mockTime);
                                break;
                            }

                            if(hasMock(tags,targetService)){
                                //添加mock的停机时间
                                addMockValidShutdownTime(key,targetService);
                                log.info("mock服务 {}:{} 的 availability",targetType,targetService);
                                falconReportObject.setValue("1");
                                falconReportObject.appendTags("mock=true");
                                isOK = true;
                                break;
                            }
                        }
                        if(isOK){
                            break;
                        }
                    }
                }
            }
        }else{
            //mock清除处理
            String serviceType = getServiceTypeByPlugin(plugin);
            removeMockService(serviceType,agentSignName);
            removeMockService(serviceType,serverName);
        }

        String time = DateUtil.getFormatDateTime(new Date(timestamp * 1000));
        log.info("Variability({}) ：{}",time,falconReportObject);
        return falconReportObject;
    }

    private static boolean hasMock(String tags,String targetService){
        log.info("判断mock需求1：【tags:{}】【target:{}】",tags,targetService);
        boolean mock = false;
        if (!StringUtils.isEmpty(tags)){
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                if(tag.contains("=")){
                    String[] ss = tag.trim().split("=");
                    String tagName = ss[0].trim();
                    String tagValue = ss[1].trim();
                    if("agentSignName".equals(tagName) || "service".equals(tagName)){
                        log.info("判断mock需求2：【tagName:{}】【tagValue:{}】",tagName,tagValue);
                        if(tagValue.contains(targetService)){
                            mock = true;
                            break;
                        }
                    }
                }
            }
        }
        return mock;
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
            log.error("获取系统Host信息失败",e);
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
                log.error("执行js表达式错误",e);
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
        signName += ",service.type=" + getServiceTypeByPlugin(plugin);
        if(metricsType != null){
            signName += ",metrics.type=" + metricsType.getTypeName();
        }
        if(StringUtils.isEmpty(agentSignName)){
            return signName;
        }
        return signName + ",agentSignName=" + agentSignName;
    }

    private static String getServiceTypeByPlugin(Plugin plugin){
        String type = "unknow";
        if(JMXPlugin.class.isAssignableFrom(plugin.getClass())){
            type = "jmx";
        }else if(JDBCPlugin.class.isAssignableFrom(plugin.getClass())){
            type = "database";
        }else if(SNMPV3Plugin.class.isAssignableFrom(plugin.getClass())){
            type = "snmp";
        }else if(DetectPlugin.class.isAssignableFrom(plugin.getClass())){
            type = "detect";
        }

        return type;
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
