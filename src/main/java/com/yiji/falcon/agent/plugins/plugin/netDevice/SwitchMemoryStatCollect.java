/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.netDevice;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-14 15:41 创建
 */

import com.yiji.falcon.agent.plugins.util.SNMPHelper;
import com.yiji.falcon.agent.plugins.util.SNMPV3Session;
import com.yiji.falcon.agent.plugins.util.VendorType;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.PDU;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 交换机的内存利用率数据采集
 *
 * @author guqiu@yiji.com
 */
@Slf4j
class SwitchMemoryStatCollect {
    private static final String metricsName = "AllMemoryUsageRatio";

    /**
     * 获取设备内存利用率数据
     *
     * @param session
     * @return
     * @throws IOException
     */
    static CollectObject getMemoryStat(SNMPV3Session session) throws IOException {
        VendorType vendor = session.getSysVendor();

        String oid = "";

        switch (vendor) {
            case Cisco_NX:
                oid = "1.3.6.1.4.1.9.9.305.1.1.2.0";
                break;
            case Cisco:
            case Cisco_IOS_XE:
            case Cisco_IOS_7200:
            case Cisco_12K: {
                String memUsedOid = "1.3.6.1.4.1.9.9.48.1.1.1.5.1";
                String memFreeOid = "1.3.6.1.4.1.9.9.48.1.1.1.6.1";

                PDU memUsedPDU = session.get(memUsedOid);
                PDU memFreePDU = session.get(memFreeOid);

                CollectObject collectObject = new CollectObject();
                collectObject.setSession(session);
                collectObject.setTime(new Date());
                collectObject.setMetrics(metricsName);
                setRatioValueHelper(memUsedPDU,memFreePDU,collectObject);

                return collectObject;
            }
            case Cisco_IOS_XR:
                return getCisco_IOS_XR_Mem(session, oid);
            case Cisco_ASA:
            case Cisco_ASA_OLD:
                return getCisco_ASA_Mem(session, oid);
            case Huawei:
            case Huawei_V5_70:
            case Huawei_V5_130:
                oid = "1.3.6.1.4.1.2011.5.25.31.1.1.1.1.7";
                return getH3CHWcpumem(metricsName,session, oid);
            case Huawei_V3_10:
                return getOldHuawei_Mem(session, oid);
            case Huawei_ME60:
                return getHuawei_Me60_Mem(session, oid);
            case H3C:
            case H3C_V5:
            case H3C_V7:
                oid = "1.3.6.1.4.1.25506.2.6.1.1.1.1.8";
                return getH3CHWcpumem(metricsName,session, oid);
            case H3C_S9500:
                oid = "1.3.6.1.4.1.2011.10.2.6.1.1.1.1.8";
                return getH3CHWcpumem(metricsName,session, oid);
            case Juniper:
                oid = "1.3.6.1.4.1.2636.3.1.13.1.11";
                return getH3CHWcpumem(metricsName,session, oid);
            case Ruijie:
                oid = "1.3.6.1.4.1.4881.1.1.10.2.35.1.1.1.3.0";
                return getRuijiecpumem(metricsName,session, oid);
            default:
                break;
        }

        CollectObject collectObject = new CollectObject();
        PDU pdu = session.get(oid);
        collectObject.setMetrics(metricsName);
        collectObject.setSession(session);
        collectObject.setValue(SNMPHelper.getValueFromPDU(pdu));
        collectObject.setTime(new Date());
        return collectObject;
    }

    static CollectObject getRuijiecpumem(String metricsName,SNMPV3Session session, String oid) throws IOException {
        CollectObject collectObject = new CollectObject();
        collectObject.setSession(session);
        collectObject.setMetrics(metricsName);
        PDU pdu = session.get(oid);
        collectObject.setValue(SNMPHelper.getValueFromPDU(pdu));
        collectObject.setTime(new Date());

        return collectObject;
    }

    private static CollectObject getHuawei_Me60_Mem(SNMPV3Session session, String oid) throws IOException {
        CollectObject collectObject = new CollectObject();
        collectObject.setSession(session);
        collectObject.setMetrics(metricsName);
        collectObject.setTime(new Date());

        String memTotalOid = "1.3.6.1.4.1.2011.6.3.5.1.1.2";
        List<PDU> memTotalPDU = session.walk(memTotalOid);
        long memTotal = 0;
        for (PDU pdu : memTotalPDU) {
            memTotal += Long.parseLong(SNMPHelper.getValueFromPDU(pdu));
        }

        String memFreeOid = "1.3.6.1.4.1.2011.6.3.5.1.1.3";
        List<PDU> memFreePDU = session.walk(memFreeOid);
        long memFree = 0;
        for (PDU pdu : memFreePDU) {
            memFree += Long.parseLong(SNMPHelper.getValueFromPDU(pdu));
        }

        if(memTotal != 0 && memFree != 0){
            collectObject.setValue(String.valueOf(((float) memTotal - (float)memFree) / (float)memTotal));
        }
        return collectObject;
    }

    private static CollectObject getOldHuawei_Mem(SNMPV3Session session, String oid) throws IOException {
        CollectObject collectObject = new CollectObject();
        collectObject.setSession(session);
        collectObject.setMetrics(metricsName);
        collectObject.setTime(new Date());

        String memTotalOid = "1.3.6.1.4.1.2011.6.1.2.1.1.2";
        List<PDU> snmpMemTotal = session.walk(memTotalOid);

        String memFreeOid = "1.3.6.1.4.1.2011.6.1.2.1.1.3";
        List<PDU> snmpMemFree = session.walk(memFreeOid);

        if(snmpMemFree.isEmpty() || snmpMemTotal.isEmpty()){
            log.warn("{} No Such Object available on this agent at this OID",session);
            return null;
        }

        int memTotal = Integer.parseInt(SNMPHelper.getValueFromPDU(snmpMemTotal.get(0)));
        int memFree = Integer.parseInt(SNMPHelper.getValueFromPDU(snmpMemFree.get(0)));
        collectObject.setValue(String.valueOf(((float) memTotal - (float)memFree) / (float)memTotal));

        return collectObject;
    }

    static CollectObject getH3CHWcpumem(String metricsName,SNMPV3Session session, String oid) throws IOException {
        CollectObject collectObject = new CollectObject();

        List<PDU> pduList = session.walk(oid);
        String value = "";
        for (PDU pdu : pduList) {
            if(pdu.get(0).getVariable().toLong() != 0){
                value = SNMPHelper.getValueFromPDU(pdu);
                break;
            }
        }
        collectObject.setSession(session);
        collectObject.setMetrics(metricsName);
        collectObject.setValue(value);
        collectObject.setTime(new Date());

        return collectObject;
    }

    private static CollectObject getCisco_ASA_Mem(SNMPV3Session session, String oid) throws IOException {
        String memUsedOid = "1.3.6.1.4.1.9.9.221.1.1.1.1.18";
        List<PDU> memUsedListPDU = session.walk(memUsedOid);
        String memFreeOid = "1.3.6.1.4.1.9.9.221.1.1.1.1.20";
        List<PDU> memFreeListPDU = session.walk(memFreeOid);

        CollectObject collectObject = new CollectObject();
        collectObject.setMetrics(metricsName);
        collectObject.setSession(session);
        collectObject.setTime(new Date());

        if(SNMPHelper.isValidPDU(memFreeListPDU.get(0)) && SNMPHelper.isValidPDU(memUsedListPDU.get(0))){
            int memUsed = Integer.parseInt(SNMPHelper.getValueFromPDU(memUsedListPDU.get(0)));
            int memFree = Integer.parseInt(SNMPHelper.getValueFromPDU(memFreeListPDU.get(0)));
            if(memUsed+memFree != 0){
                collectObject.setValue(String.valueOf((double)memUsed / ((double)memUsed + (double)memFree)));
            }
        }

        return collectObject;
    }

    private static CollectObject getCisco_IOS_XR_Mem(SNMPV3Session session, String oid) throws IOException {
        CollectObject collectObject = new CollectObject();
        collectObject.setMetrics(metricsName);
        collectObject.setSession(session);
        collectObject.setTime(new Date());

        String cpuIndex = "1.3.6.1.4.1.9.9.109.1.1.1.1.2";
        PDU cpuIndexPDU = session.getNext(cpuIndex);
        if(cpuIndexPDU.get(0) != null){
            int index  = Integer.parseInt(SNMPHelper.getValueFromPDU(cpuIndexPDU));
            String memUsedOid = "1.3.6.1.4.1.9.9.221.1.1.1.1.18." + index + ".1";
            String memFreeOid = "1.3.6.1.4.1.9.9.221.1.1.1.1.20." + index + ".1";
            PDU memUsedPDU = session.get(memUsedOid);
            PDU memFreePDU = session.get(memFreeOid);
            setRatioValueHelper(memUsedPDU,memFreePDU,collectObject);
        }

        return collectObject;
    }

    /**
     * 使用率设值
     * @param usedPDU
     * @param freePDU
     * @param collectObject
     */
    private static void setRatioValueHelper(PDU usedPDU, PDU freePDU, CollectObject collectObject){
        if(SNMPHelper.isValidPDU(usedPDU) && SNMPHelper.isValidPDU(freePDU)){
            int used = Integer.parseInt(SNMPHelper.getValueFromPDU(usedPDU));
            int free = Integer.parseInt(SNMPHelper.getValueFromPDU(freePDU));
            if (free + used != 0){
                collectObject.setValue(String.valueOf((float) used / ((float)used + (float)free)));
            }
        }
    }
}
