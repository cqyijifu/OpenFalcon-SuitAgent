/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.netDevice;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-14 10:09 创建
 */

import com.yiji.falcon.agent.plugins.util.SNMPHelper;
import com.yiji.falcon.agent.plugins.util.SNMPV3Session;
import com.yiji.falcon.agent.plugins.util.VendorType;
import org.snmp4j.PDU;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 交换机的CPU监控数据采集
 *
 * @author guqiu@yiji.com
 */
class SwitchCPUStatCollect {

    private final static String metricsName = "AllCpuUsageRatio";

    /**
     * 获取设备CPU利用率数据
     * @param session
     * @return
     * @throws IOException
     */
    static CollectObject getCPUStat(SNMPV3Session session) throws IOException {
        VendorType vendor = session.getSysVendor();

        String method = "get";
        String oid = "";

        switch (vendor) {
            case Cisco_NX:
                oid = "1.3.6.1.4.1.9.9.305.1.1.1.0";
                break;
            case Cisco:
            case Cisco_IOS_7200:
            case Cisco_12K:
                oid = "1.3.6.1.4.1.9.9.109.1.1.1.1.7.1";
                break;
            case Cisco_IOS_XE:
            case Cisco_IOS_XR:
                oid = "1.3.6.1.4.1.9.9.109.1.1.1.1.7";
                method = "getnext";
                break;
            case Cisco_ASA:
                oid = "1.3.6.1.4.1.9.9.109.1.1.1.1.7";
                return getCiscoASAcpu(session,oid);
            case Cisco_ASA_OLD:
                oid = "1.3.6.1.4.1.9.9.109.1.1.1.1.4";
                return getCiscoASAcpu(session,oid);
            case Huawei:
            case Huawei_V5_70:
            case Huawei_V5_130:
                oid = "1.3.6.1.4.1.2011.5.25.31.1.1.1.1.5";
                //算法与内存的获取一样
                return SwitchMemoryStatCollect.getH3CHWcpumem(session,oid);
            case Huawei_V3_10:
                oid = "1.3.6.1.4.1.2011.6.1.1.1.3";
                //算法与内存的获取一样
                return SwitchMemoryStatCollect.getH3CHWcpumem(session,oid);
            case Huawei_ME60:
                oid = "1.3.6.1.4.1.2011.6.3.4.1.2";
                return getHuawei_ME60cpu(session,oid);
            case H3C:
            case H3C_V5:
            case H3C_V7:
                oid = "1.3.6.1.4.1.25506.2.6.1.1.1.1.6";
                //算法与内存的获取一样
                return SwitchMemoryStatCollect.getH3CHWcpumem(session,oid);
            case H3C_S9500:
                oid = "1.3.6.1.4.1.2011.10.2.6.1.1.1.1.6";
                //算法与内存的获取一样
                return SwitchMemoryStatCollect.getH3CHWcpumem(session,oid);
            case Juniper:
                oid = "1.3.6.1.4.1.2636.3.1.13.1.8";
                //算法与内存的获取一样
                return SwitchMemoryStatCollect.getH3CHWcpumem(session,oid);
            case Ruijie:
                oid = "1.3.6.1.4.1.4881.1.1.10.2.36.1.1.2.0";
                //算法与内存的获取一样
                return SwitchMemoryStatCollect.getRuijiecpumem(session,oid);
            default:
                break;
        }

        CollectObject collectObject = new CollectObject();
        PDU pdu;
        if ("get".equals(method)) {
            pdu = session.get(oid);
        }else{
            pdu = session.getNext(oid);
        }
        collectObject.setMetrics(metricsName);
        collectObject.setSession(session);
        collectObject.setValue(SNMPHelper.getValueFromPDU(pdu));
        collectObject.setTime(new Date());
        return collectObject;
    }

    private static CollectObject getHuawei_ME60cpu(SNMPV3Session session, String oid) throws IOException {
        return walkForSumDivCount(session,oid);
    }

    private static CollectObject getCiscoASAcpu(SNMPV3Session session, String oid) throws IOException {
        return walkForSumDivCount(session,oid);
    }

    /**
     * walk的方式获取监控值
     * 通过获得的总值除数量的逻辑
     * @param session
     * @param oid
     * @return
     * @throws IOException
     */
    private static CollectObject walkForSumDivCount(SNMPV3Session session, String oid) throws IOException {
        CollectObject collectObject = new CollectObject();
        List<PDU> pduList = session.walk(oid);

        int count = pduList.size();
        long value = 0;

        for (PDU pdu : pduList) {
            value += Long.parseLong(SNMPHelper.getValueFromPDU(pdu));
        }

        collectObject.setSession(session);
        collectObject.setMetrics(metricsName);
        collectObject.setValue(0 + "");
        if(count > 0){
            collectObject.setValue(String.valueOf(value / count));
        }
        collectObject.setTime(new Date());

        return collectObject;
    }

}
