/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.netDevice;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-08 14:28 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.SNMPV3Plugin;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.plugins.util.SNMPV3Session;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 交换机设备监控插件
 * @author guqiu@yiji.com
 */
public class SwitchPlugin implements SNMPV3Plugin{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int step;
    private PluginActivateType pluginActivateType;
    private List<String> switchUrlList;
    private String ifCollect;
    private Map<String,Boolean> ifCollectMetricsEnable;

    @Override
    public String authorizationKeyPrefix() {
        return "switch";
    }

    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     * @param properties
     * 包含的配置:
     * 1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     * 2、插件指定的配置文件的全部配置信息(参见 {@link com.yiji.falcon.agent.plugins.Plugin#configFileName()} 接口项)
     * 3、授权配置项(参见 {@link com.yiji.falcon.agent.plugins.Plugin#authorizationKeyPrefix()} 接口项
     */
    @Override
    public void init(Map<String, String> properties) {
        this.ifCollect = properties.get("enable.if.Collect");
        step = Integer.parseInt(properties.get("step"));
        pluginActivateType = PluginActivateType.valueOf(properties.get("pluginActivateType"));
        switchUrlList = new ArrayList<>();
        ifCollectMetricsEnable = new HashMap<>();
        properties.keySet().forEach(key -> {
            if (key.startsWith("switch.url")) {
                switchUrlList.add(properties.get(key));
            }
            if (key.startsWith("if.")) {
                ifCollectMetricsEnable.put(key, "true".equals(properties.get(key)));
            }
        });
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        //该插件监控的都是交换机设备
        return "switch";
    }

    /**
     * 监控值的获取和上报周期(秒)
     *
     * @return
     */
    @Override
    public int step() {
        return step;
    }

    /**
     * 插件运行方式
     *
     * @return
     */
    @Override
    public PluginActivateType activateType() {
        return pluginActivateType;
    }

    /**
     * Agent关闭时的调用钩子
     * 如，可用于插件的资源释放等操作
     */
    @Override
    public void agentShutdownHook() {

    }

    /**
     * 通过SNMPV3协议获取设备监控信息的登陆用户信息
     *
     * @return list
     * 一个 {@link com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo} 对象就是一个设备的监控
     */
    @Override
    public Collection<SNMPV3UserInfo> userInfo() {
        List<SNMPV3UserInfo> userInfoList = new ArrayList<>();
        for (String switchUrl : switchUrlList) {
            if(!StringUtils.isEmpty(switchUrl)){
                String[] urls = switchUrl.split(",");
                for (String url : urls) {
                    if(!StringUtils.isEmpty(url) && url.contains("snmpv3://")){
                        //snmpv3://protocol:address:port:username:authType:authPswd:privType:privPswd
                        url = url.replace("snmpv3://","");
                        String[] props = url.split(":");
                        if(props.length < 8){
                            logger.error("snmp v3 的连接URL格式错误,请检查URL:{} 是否符合格式:snmpv3://protocol:address:port:username:authType:authPswd:privType:privPswd:endPoint(option)",url);
                            continue;
                        }
                        SNMPV3UserInfo userInfo = new SNMPV3UserInfo();
                        userInfo.setProtocol(props[0]);
                        userInfo.setAddress(props[1]);
                        userInfo.setPort(props[2]);
                        userInfo.setUsername(props[3]);
                        userInfo.setAythType(props[4]);
                        userInfo.setAuthPswd(props[5]);
                        userInfo.setPrivType(props[6]);
                        userInfo.setPrivPswd(props[7]);
                        if(props.length >= 9){
                            userInfo.setEndPoint(props[8].trim());
                        }
                        if(props.length >= 10){
                            String ifEnable = props[9].trim();
                            List<String> enableList = new ArrayList<>();
                            Collections.addAll(enableList, ifEnable.split("&"));
                            userInfo.setIfCollectNameEnables(enableList.stream().filter(str -> !StringUtils.isEmpty(str)).collect(Collectors.toList()));
                        }

                        userInfoList.add(userInfo);
                    }
                }
            }
        }
        return userInfoList;
    }

    /**
     * 当设备链接可用时的插件内置报告,如该插件适配的不同设备和品牌的SNMP监控报告
     * Agent会自动采集一些公共的MIB数据,但是设备私有的MIB信息,将有不同的插件自己提供
     *
     * @param session 连接设备的SNMP Session,插件可通过此对象进行设备间的SNMP通信,以获取监控数据
     * @return
     */
    @Override
    public Collection<FalconReportObject> inbuiltReportObjectsForValid(SNMPV3Session session) {
        List<CollectObject> collectObjects = new ArrayList<>();

        try {
            collectObjects.add(SwitchCPUStatCollect.getCPUStat(session));
            collectObjects.add(SwitchMemoryStatCollect.getMemoryStat(session));
        } catch (Exception e) {
            logger.error("获取设备 {} 的CPU利用率数据失败",session.toString(),e);
        }

        final List<FalconReportObject> falconReportObjects = new ArrayList<>();
        collectObjects.forEach(collectObject -> {
            if(collectObject != null){
                FalconReportObject reportObject = new FalconReportObject();
                MetricsCommon.setReportCommonValue(reportObject, this.step());
                reportObject.appendTags(MetricsCommon.getTags(collectObject.getSession().getAgentSignName(), this, this.serverName(), MetricsType.SNMP_Plugin_IN_BUILD));
                reportObject.setCounterType(CounterType.GAUGE);
                reportObject.setMetric(collectObject.getMetrics());
                reportObject.setValue(collectObject.getValue());
                reportObject.setTimestamp(collectObject.getTime().getTime() / 1000);
                falconReportObjects.add(reportObject);
            }
        });
        return falconReportObjects;
    }

    /**
     * 是否需要进行接口数据采集
     *
     * @return
     */
    @Override
    public boolean hasIfCollect() {
        return "true".equals(this.ifCollect);
    }

    /**
     * 允许采集哪些接口数据，用map的形式返回，key为接口metrics名称，value为是否允许。
     * 若对应key的metrics获取为null或false，均不采集。key的集合为：
     * <p>
     * if.HCInBroadcastPkts
     * if.HCInMulticastPkts
     * if.HCInOctets
     * if.HCInUcastPkts
     * if.HCOutBroadcastPkts
     * if.HCOutMulticastPkts
     * if.getIfHCOutUcastPkts
     * if.OperStatus
     * if.HCOutOctets
     *
     * @return
     */
    @Override
    public Map<String, Boolean> ifCollectMetricsEnable() {
        return this.ifCollectMetricsEnable;
    }

}
