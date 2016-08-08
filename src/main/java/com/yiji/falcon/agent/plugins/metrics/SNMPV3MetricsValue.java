/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-01 17:29 创建
 */

import com.yiji.falcon.agent.exception.AgentArgumentException;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.SNMPV3Plugin;
import com.yiji.falcon.agent.plugins.util.SNMPHelper;
import com.yiji.falcon.agent.plugins.util.SNMPV3Session;
import com.yiji.falcon.agent.util.CommandUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.snmp.IfStatVO;
import com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.yiji.falcon.agent.plugins.util.SNMPHelper.ignoreIfName;

/**
 * @author guqiu@yiji.com
 */
public class SNMPV3MetricsValue extends MetricsCommon {

    private static final Logger logger = LoggerFactory.getLogger(SNMPV3MetricsValue.class);

    private static final ConcurrentHashMap<String, List<SNMPV3Session>> sessionCache = new ConcurrentHashMap<>();

    private SNMPV3Plugin plugin;

    public SNMPV3MetricsValue(SNMPV3Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 关闭所有的SNMP连接
     */
    public static void closeAllSession() {
        for (List<SNMPV3Session> sessions : sessionCache.values()) {
            for (SNMPV3Session session : sessions) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * 获取SNMP的session
     * 首先从缓存中获取,若获取失败,则进行缓存对象的创建,并加入缓存
     *
     * @return
     * @throws IOException
     * @throws AgentArgumentException
     */
    public List<SNMPV3Session> getSessions() throws IOException, AgentArgumentException {
        List<SNMPV3Session> sessions = sessionCache.get(plugin.pluginName());
        if (sessions == null || sessions.isEmpty()) {
            sessions = new ArrayList<>();
            Collection<SNMPV3UserInfo> userInfoCollection = plugin.userInfo();
            if(userInfoCollection != null){
                for (SNMPV3UserInfo userInfo : userInfoCollection) {
                    SNMPV3Session session = new SNMPV3Session(userInfo);
                    sessions.add(session);
                }
                sessionCache.put(plugin.pluginName(), sessions);
            }
        }
        return sessions;
    }

    /**
     * 设备的接口监控数据
     * @param session
     * @return
     * @throws IOException
     */
    public Collection<FalconReportObject> getIfStatReports(SNMPV3Session session) throws IOException {
        Set<FalconReportObject> reportObjects = new HashSet<>();

        List<PDU> ifNameList = session.walk(SNMPHelper.ifNameOid);
        List<PDU> ifInList = session.walk(SNMPHelper.ifHCInOid);
        List<PDU> ifOutList = session.walk(SNMPHelper.ifHCOutOid);
        List<PDU> ifHCInPktsList = session.walk(SNMPHelper.ifHCInPktsOid);
        List<PDU> ifHCOutPktsList = session.walk(SNMPHelper.ifHCOutPktsOid);
        List<PDU> ifOperStatusList = session.walk(SNMPHelper.ifOperStatusOid);
        List<PDU> ifHCInBroadcastPktsList = session.walk(SNMPHelper.ifHCInBroadcastPktsOid);
        List<PDU> ifHCOutBroadcastPktsList = session.walk(SNMPHelper.ifHCOutBroadcastPktsOid);
        List<PDU> ifHCInMulticastPktsList = session.walk(SNMPHelper.ifHCInMulticastPktsOid);
        List<PDU> ifHCOutMulticastPktsList = session.walk(SNMPHelper.ifHCOutMulticastPktsOid);

        List<IfStatVO> statVOs = new ArrayList<>();

        for (PDU pdu : ifNameList) {
            VariableBinding ifName = pdu.get(0);
            boolean check = true;
            for (String ignore : ignoreIfName) {
                if (ifName.getVariable().toString().contains(ignore)) {
                    check = false;
                    break;
                }
            }
            if (check) {
                int index = Integer.parseInt(ifName.getOid().toString().replace(SNMPHelper.ifNameOid, "").replace(".", ""));
                IfStatVO statVO = new IfStatVO();
                statVO.setIfName(ifName.getVariable().toString());
                statVO.setIfIndex(index);
                statVO.setIfHCInBroadcastPkts(SNMPHelper.getValueFromPDU(ifHCInBroadcastPktsList.get(index)));
                statVO.setIfHCInMulticastPkts(SNMPHelper.getValueFromPDU(ifHCInMulticastPktsList.get(index)));
                statVO.setIfHCInOctets(SNMPHelper.getValueFromPDU(ifInList.get(index)));
                statVO.setIfHCOutOctets(SNMPHelper.getValueFromPDU(ifOutList.get(index)));
                statVO.setIfHCInUcastPkts(SNMPHelper.getValueFromPDU(ifHCInPktsList.get(index)));
                statVO.setIfHCOutUcastPkts(SNMPHelper.getValueFromPDU(ifHCOutPktsList.get(index)));
                statVO.setIfOperStatus(SNMPHelper.getValueFromPDU(ifOperStatusList.get(index)));
                statVO.setIfHCOutBroadcastPkts(SNMPHelper.getValueFromPDU(ifHCOutBroadcastPktsList.get(index)));
                statVO.setIfHCOutMulticastPkts(SNMPHelper.getValueFromPDU(ifHCOutMulticastPktsList.get(index)));
                statVO.setTime(new Date());

                statVOs.add(statVO);
            }
        }

        for (IfStatVO statVO : statVOs) {
            FalconReportObject reportObject = new FalconReportObject();
            MetricsCommon.setReportCommonValue(reportObject, plugin.step());
            reportObject.appendTags(MetricsCommon.getTags(session.getEquipmentName(), plugin, plugin.serverName(), MetricsType.SNMP_COMMON_IN_BUILD));
            reportObject.setCounterType(CounterType.GAUGE);
            String endPoint = session.getUserInfo().getEndPoint();
            if(!StringUtils.isEmpty(endPoint)){
                //设置单独设置的endPoint
                reportObject.setEndpoint(endPoint);
                reportObject.appendTags("customerEndPoint=true");
            }

            String ifName = statVO.getIfName();
            long time = statVO.getTime().getTime() / 1000;

            reportObject.setMetric(String.format("%s.if.HCInBroadcastPkts", ifName));
            reportObject.setValue(statVO.getIfHCInBroadcastPkts());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.HCInMulticastPkts", ifName));
            reportObject.setValue(statVO.getIfHCInMulticastPkts());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.HCInOctets", ifName));
            reportObject.setValue(statVO.getIfHCInOctets());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.HCInUcastPkts", ifName));
            reportObject.setValue(statVO.getIfHCInUcastPkts());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.HCOutBroadcastPkts", ifName));
            reportObject.setValue(statVO.getIfHCOutBroadcastPkts());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.HCOutMulticastPkts", ifName));
            reportObject.setValue(statVO.getIfHCOutMulticastPkts());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.getIfHCOutUcastPkts", ifName));
            reportObject.setValue(statVO.getIfHCOutUcastPkts());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.OperStatus", ifName));
            reportObject.setValue(statVO.getIfOperStatus());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

            reportObject.setMetric(String.format("%s.if.HCOutOctets", ifName));
            reportObject.setValue(statVO.getIfHCOutOctets());
            reportObject.setTimestamp(time);
            reportObjects.add(reportObject.clone());

        }

        return reportObjects;
    }

    /**
     * ping操作
     * @param session
     * @param count
     * @return
     */
    public FalconReportObject ping(SNMPV3Session session,int count) {
        FalconReportObject reportObject = new FalconReportObject();
        MetricsCommon.setReportCommonValue(reportObject, plugin.step());
        reportObject.appendTags(MetricsCommon.getTags(session.getEquipmentName(), plugin, plugin.serverName(), MetricsType.SNMP_COMMON_IN_BUILD));
        reportObject.setCounterType(CounterType.GAUGE);
        reportObject.setMetric("pingAvgTime");
        reportObject.setTimestamp(System.currentTimeMillis() / 1000);

        String address = session.getUserInfo().getAddress();

        try {
            Double time = CommandUtil.ping(address,count);
            if(time == -2){
                //命令执行失败
                return null;
            }
            reportObject.setValue(time + "");
        } catch (IOException e) {
            logger.error("Ping {} 命令执行异常",address,e);
            return null;
        }
        return reportObject;
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
        List<SNMPV3Session> sessionList = null;
        try {
            sessionList = getSessions();
        } catch (IOException e) {
            logger.warn("获取SNMP连接发生异常,push allUnVariability不可用报告", e);
            result.add(MetricsCommon.generatorVariabilityReport(false, "allUnVariability", plugin.step(), plugin, plugin.serverName()));
            return result;

        } catch (AgentArgumentException e) {
            logger.error("监控参数异常:{},忽略此监控上报",e.getErr(), e);
            return result;
        }

        for (SNMPV3Session session : sessionList) {
            //ping报告
            FalconReportObject reportObject = ping(session,5);
            if(reportObject != null){
                result.add(reportObject);
            }
            try {
                result.addAll(getIfStatReports(session));
                //添加可用性报告
                result.add(MetricsCommon.generatorVariabilityReport(true, session.getEquipmentName(), plugin.step(), plugin, plugin.serverName()));
                //添加插件报告
                Collection<FalconReportObject> inBuildReports = plugin.inbuiltReportObjectsForValid(sessionList);
                if(inBuildReports != null && !inBuildReports.isEmpty()){
                    result.addAll(inBuildReports);
                }
            } catch (IOException e) {
                logger.error("设备 {} 通过SNMP获取监控数据发生异常,push 该设备不可用报告",session.toString(),e);
                result.add(MetricsCommon.generatorVariabilityReport(false, "allUnVariability", plugin.step(), plugin, plugin.serverName()));
            }
        }

        return result;
    }
}
