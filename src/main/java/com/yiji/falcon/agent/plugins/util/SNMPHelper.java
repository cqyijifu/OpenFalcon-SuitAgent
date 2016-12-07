/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-11 16:29 创建
 */

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class SNMPHelper {

    /* 公共MIB的接口OID定义 */
    public static final String ifNameOid = "1.3.6.1.2.1.31.1.1.1.1";
    public static final String ifHCInOid = "1.3.6.1.2.1.31.1.1.1.6";
    public static final String ifHCOutOid = "1.3.6.1.2.1.31.1.1.1.10";
    public static final String ifHCInPktsOid = "1.3.6.1.2.1.31.1.1.1.7";
    public static final String ifHCOutPktsOid = "1.3.6.1.2.1.31.1.1.1.11";
    public static final String ifOperStatusOid = "1.3.6.1.2.1.2.2.1.8";
    public static final String ifHCInBroadcastPktsOid = "1.3.6.1.2.1.31.1.1.1.9";
    public static final String ifHCOutBroadcastPktsOid = "1.3.6.1.2.1.31.1.1.1.13";
    public static final String ifHCInMulticastPktsOid = "1.3.6.1.2.1.31.1.1.1.8";
    public static final String ifHCOutMulticastPktsOid = "1.3.6.1.2.1.31.1.1.1.12";
    /* 忽略的接口，如Nu匹配ifName为*Nu*的接口 */
    public static final List<String> ignoreIfName = Arrays.asList("Nu", "NU", "Vlan", "Vl", "LoopBack");

    public static final String sysDescOid = "1.3.6.1.2.1.1.1.0";

    /**
     * 判断传入的PDU是否带有可用的监控数据(PDU携带的第一个VariableBinding对象)
     * @param pdu
     * @return
     * true : 可用
     * false : 不可用
     */
    public static boolean isValidPDU(PDU pdu){
        if(pdu == null){
            return false;
        }
        VariableBinding vb = pdu.get(0);
        if(vb == null){
            return false;
        }
        String vbResult = vb.toString();
        return !"noSuchInstance".equalsIgnoreCase(vbResult) &&
                !"noSuchObject".equalsIgnoreCase(vbResult) &&
                !"noNextInstance".equalsIgnoreCase(vbResult) &&
                !"endOfView".equalsIgnoreCase(vbResult);
    }

    /**
     * 判断传入的PDU是否带有可用的监控数据
     * @param pdu
     * @param index
     * 指定的索引的VariableBinding对象
     * @return
     * true : 可用
     * false : 不可用
     */
    public static boolean isValidPDU(PDU pdu,int index){
        if(pdu == null){
            return false;
        }
        VariableBinding vb = pdu.get(index);
        if(vb == null){
            return false;
        }
        String vbResult = vb.toString();
        return !"noSuchInstance".equalsIgnoreCase(vbResult) &&
                !"noSuchObject".equalsIgnoreCase(vbResult) &&
                !"noNextInstance".equalsIgnoreCase(vbResult) &&
                !"endOfView".equalsIgnoreCase(vbResult);
    }

    /**
     * 获取传入PDU携带的MIB值(PDU携带的第一个VariableBinding对象)
     * @param pdu
     * @return
     * 若PDU无效返回空串
     */
    public static String getValueFromPDU(PDU pdu){
        if(isValidPDU(pdu)){
            return pdu.get(0).getVariable().toString();
        }
        return "";
    }

    /**
     * 获取传入PDU携带的MIB值
     * @param pdu
     * @param index
     * 指定的VariableBinding对象
     * @return
     * 若PDU无效返回空串
     */
    public static String getValueFromPDU(PDU pdu,int index){
        if(isValidPDU(pdu,index)){
            return pdu.get(index).getVariable().toString();
        }
        return "";
    }

    /**
     * 获取指定OID的 get
     *
     * @param snmp
     * @param target
     * @param oid
     * @return
     * @throws IOException
     */
    public static PDU snmpGet(Snmp snmp, Target target, String oid) throws IOException {
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(new OID(oid)));

        ResponseEvent responseEvent = snmp.send(pdu, target);
        PDU response = responseEvent.getResponse();
        if(response == null){
            log.warn("response null - error:{} peerAddress:{} source:{} request:{}",
                    responseEvent.getError(),
                    responseEvent.getPeerAddress(),
                    responseEvent.getSource(),
                    responseEvent.getRequest());
        }
        return response;
    }

    /**
     * 获取指定OID的 getNext
     *
     * @param snmp
     * @param target
     * @param oid
     * @return
     * @throws IOException
     */
    public static PDU snmpGetNext(Snmp snmp, Target target, String oid) throws IOException {
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.GETNEXT);
        pdu.add(new VariableBinding(new OID(oid)));

        ResponseEvent responseEvent = snmp.send(pdu, target);
        return responseEvent.getResponse();
    }

    /**
     * walk方式获取指定的oid value
     *
     * @param snmp
     * @param target
     * @param oid
     * @return
     * @throws IOException
     */
    public static List<PDU> snmpWalk(Snmp snmp, Target target, String oid) throws IOException {
        List<PDU> pduList = new ArrayList<>();

        ScopedPDU pdu = new ScopedPDU();
        OID targetOID = new OID(oid);
        pdu.add(new VariableBinding(targetOID));

        boolean finished = false;
        while (!finished) {
            VariableBinding vb = null;
            ResponseEvent respEvent = snmp.getNext(pdu, target);

            PDU response = respEvent.getResponse();

            if (null == response) {
                break;
            } else {
                vb = response.get(0);
            }
            // check finish
            finished = checkWalkFinished(targetOID, pdu, vb);
            if (!finished) {
                pduList.add(response);

                // Set up the variable binding for the next entry.
                pdu.setRequestID(new Integer32(0));
                pdu.set(0, vb);
            }
        }

        return pduList;
    }

    private static boolean checkWalkFinished(OID targetOID, PDU pdu, VariableBinding vb) {
        boolean finished = false;
        if (pdu.getErrorStatus() != 0) {
            finished = true;
        } else if (vb.getOid() == null) {
            finished = true;
        } else if (vb.getOid().size() < targetOID.size()) {
            finished = true;
        } else if (targetOID.leftMostCompare(targetOID.size(), vb.getOid()) != 0) {
            finished = true;
        } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
            finished = true;
        } else if (vb.getOid().compareTo(targetOID) <= 0) {
            finished = true;
        }
        return finished;

    }

    /**
     * 根据系统信息获取供应商
     *
     * @param sysDesc
     * @return
     */
    public static VendorType getVendorBySysDesc(String sysDesc) {
        String sysDescLower = sysDesc.toLowerCase();

        if (sysDescLower.contains("cisco nx-os")) {
            return VendorType.Cisco_NX;
        }

        if (sysDesc.contains("Cisco Internetwork Operating System Software") && sysDesc.contains("C12K")) {
            return VendorType.Cisco_12K;
        }

        if (sysDescLower.contains("cisco ios")) {
            if (sysDesc.contains("IOS-XE Software")) {
                return VendorType.Cisco_IOS_XE;
            } else if (sysDesc.contains("Cisco IOS XR")) {
                return VendorType.Cisco_IOS_XR;
            } else {
                return VendorType.Cisco;
            }
        }

        if (sysDescLower.contains("cisco adaptive security appliance")) {
            float version = getVersionBySysDesc(sysDesc);
            if (version != 0 && version < 9.2) {
                return VendorType.Cisco_ASA_OLD;
            }
            return VendorType.Cisco_ASA;
        }

        if (sysDescLower.contains("cisco internetwork operating system software") &&
                sysDescLower.contains("7200 software")) {
            return VendorType.Cisco_IOS_7200;
        }

        if (sysDescLower.contains("h3c")) {
            if (sysDesc.contains("Software Version 5")) {
                return VendorType.H3C_V5;
            } else if (sysDesc.contains("Software Version 7")) {
                return VendorType.H3C_V7;
            } else if (sysDesc.contains("Version S9500")) {
                return VendorType.H3C_S9500;
            }
            return VendorType.H3C;
        }

        if (sysDescLower.contains("huawei")) {
            if (sysDesc.contains("MultiserviceEngine 60")) {
                return VendorType.Huawei_ME60;
            } else if (sysDesc.contains("Version 5.70")) {
                return VendorType.Huawei_V5_70;
            } else if (sysDesc.contains("Version 5.130")) {
                return VendorType.Huawei_V5_130;
            } else if (sysDesc.contains("Version 3.10")) {
                return VendorType.Huawei_V3_10;
            }
            return VendorType.Huawei;
        }

        if (sysDescLower.contains("ruijie")) {
            return VendorType.Ruijie;
        }

        if (sysDescLower.contains("juniper networks")) {
            return VendorType.Juniper;
        }

        if (sysDescLower.contains("linux")) {
            return VendorType.Linux;
        }

        return VendorType.UNKNOWN;
    }

    /**
     * 根据系统描述信息获取版本号
     *
     * @param sysDesc
     * @return
     */
    public static float getVersionBySysDesc(String sysDesc) {
        float version = 0;
        String versionStr = "";
        String[] ss = sysDesc.split("\\s+");
        for (int i = 0; i < ss.length; i++) {
            if ("version".equals(ss[i].toLowerCase())) {
                versionStr = ss[i + 1];
            }
        }
        versionStr = versionStr.replace("(", "").replace(")", "");

        try {
            version = Float.parseFloat(versionStr);
        } catch (NumberFormatException ignored) {
        }

        return version;
    }

}
