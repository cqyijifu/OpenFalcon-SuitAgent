/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-12 10:47 创建
 */

import com.yiji.falcon.agent.exception.AgentArgumentException;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guqiu@yiji.com
 */
public class SNMPV3Session {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //信息缓存
    private ConcurrentHashMap<String,Object> infoCache = new ConcurrentHashMap<>();
    private final String cacheKey_equipmentName = "equipmentName";
    private final String cacheKey_equipmentNameDone = "equipmentNameDone";
    private final String cacheKey_sysDesc = "sysDesc";
    private final String cacheKey_sysVendor = "sysVendor";
    private final String cacheKey_version = "version";

    private Snmp snmp;
    private UserTarget target;
    private SNMPV3UserInfo userInfo;

    @Override
    public String toString() {
        return "SNMPV3Session{" +
                "snmp=" + snmp +
                ", target=" + target +
                ", userInfo=" + userInfo +
                '}';
    }

    /**
     * 创建SNMPV3会话
     * @param userInfo
     * @throws IOException
     * @throws AgentArgumentException
     */
    public SNMPV3Session(SNMPV3UserInfo userInfo) throws IOException, AgentArgumentException {
        if(StringUtils.isEmpty(userInfo.getAddress())){
            throw new AgentArgumentException("SNMPV3Session创建失败:snmp v3协议的访问地址不能为空");
        }
        if(StringUtils.isEmpty(userInfo.getUsername())){
            throw new AgentArgumentException("SNMPV3Session创建失败:snmp v3协议的访问用户名不能为空");
        }
        if(!StringUtils.isEmpty(userInfo.getAythType()) && StringUtils.isEmpty(userInfo.getAuthPswd())){
            throw new AgentArgumentException("SNMPV3Session创建失败:snmp v3协议指定了认证算法 aythType,就必须要指定认证密码");
        }
        if(!StringUtils.isEmpty(userInfo.getPrivType()) && StringUtils.isEmpty(userInfo.getPrivPswd())){
            throw new AgentArgumentException("SNMPV3Session创建失败:snmp v3协议指定了加密算法 privType,就必须要指定加密密码");
        }

        this.userInfo = userInfo;
        snmp = new Snmp(new DefaultUdpTransportMapping());
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.listen();

        UsmUser user = new UsmUser(
                new OctetString(userInfo.getUsername()),
                getAuthProtocol(userInfo.getAythType()), new OctetString(userInfo.getAuthPswd()),
                getPrivProtocol(userInfo.getPrivType()), new OctetString(userInfo.getPrivPswd()));

        snmp.getUSM().addUser(new OctetString(userInfo.getUsername()), user);

        target = new UserTarget();
        target.setSecurityName(new OctetString(userInfo.getUsername()));
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setAddress(GenericAddress.parse(userInfo.getProtocol() + ":" + userInfo.getAddress() + "/" + userInfo.getPort()));
        target.setTimeout(1500);
        target.setRetries(1);
    }

    /**
     * 获取该会话是否可用
     * @return
     */
    public boolean isValid(){
        try {
            return SNMPHelper.snmpGet(snmp,target,SNMPHelper.sysDescOid) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取设备名称
     * ip-vendor-version
     *
     * @return
     */
    public String getEquipmentName() {

        if("true".equals(infoCache.get(cacheKey_equipmentNameDone))){
            //已有有效的设备名称缓存,直接返回
            return (String) infoCache.get(cacheKey_equipmentName);
        }
        String prefix = this.getUserInfo().getAddress() + "-" + this.getUserInfo().getPort();
        try {
            if(isValid()){
                String name = prefix + "-" + getSysVendor() + "-" + getSysVersion();
                infoCache.put(cacheKey_equipmentName,name);
                infoCache.put(cacheKey_equipmentNameDone,"true");
                return name;
            }else{
                infoCache.put(cacheKey_equipmentNameDone,"false");
                return prefix;
            }
        } catch (IOException e) {
            logger.error("设备描述信息获取失败",e);
            infoCache.put(cacheKey_equipmentNameDone,"false");
            return prefix;
        }
    }

    /**
     * 获取设备描述字符串
     *
     * @return
     * @throws IOException
     */
    public String getSysDesc() throws IOException {
        String sysDesc = (String) infoCache.get(cacheKey_sysDesc);
        if(sysDesc != null){
            return sysDesc;
        }
        sysDesc = this.get(SNMPHelper.sysDescOid).get(0).getVariable().toString();
        infoCache.put(cacheKey_sysDesc,sysDesc);
        return sysDesc;
    }

    /**
     * 获取设备的供应商
     * @return
     * @throws IOException
     */
    public VendorType getSysVendor() throws IOException {
        VendorType sysVendor = (VendorType) infoCache.get(cacheKey_sysVendor);
        if(sysVendor != null){
            return sysVendor;
        }
        sysVendor = SNMPHelper.getVendorBySysDesc(getSysDesc());
        infoCache.put(cacheKey_sysVendor,sysVendor);
        return sysVendor;
    }

    /**
     * 获取设备版本
     * @return
     * @throws IOException
     */
    public float getSysVersion() throws IOException {
        Float version = (Float) infoCache.get(cacheKey_version);
        if(version != null){
            return version;
        }
        version = SNMPHelper.getVersionBySysDesc(getSysDesc());
        infoCache.put(cacheKey_version,version);
        return version;
    }

    /**
     * get方法获取指定OID
     * @param oid
     * @return
     * @throws IOException
     */
    public PDU get(String oid) throws IOException {
        return SNMPHelper.snmpGet(snmp,target,oid);
    }

    /**
     * getNext方法获取指定OID
     * @param oid
     * @return
     * @throws IOException
     */
    public PDU getNext(String oid) throws IOException {
        return SNMPHelper.snmpGetNext(snmp,target,oid);
    }

    /**
     * 对指定的OID进行子树walk,并返回所有的walk结果
     * @param oid
     * @return
     * @throws IOException
     */
    public List<PDU> walk(String oid) throws IOException {
        return SNMPHelper.snmpWalk(snmp,target,oid);
    }

    /**
     * 获取指定的认证算法
     * @param privType
     * @return
     * @throws AgentArgumentException
     */
    private OID getPrivProtocol(String privType) throws AgentArgumentException {

        if (privType == null
                || privType.equalsIgnoreCase("none")
                || privType.length() == 0) {
            return null;
        }

        switch (privType) {
            case "DES":
                return PrivDES.ID;
            case "3DES":
                return Priv3DES.ID;
            case "AES128":
            case "AES-128":
            case "AES":
                return PrivAES128.ID;
            case "AES192":
            case "AES-192":
                return PrivAES192.ID;
            case "AES256":
            case "AES-256":
                return PrivAES256.ID;
            default:
                throw new AgentArgumentException("Privacy protocol " + privType + " not supported");
        }
    }

    /**
     * 获取指定的加密算法
     * @param authMethod
     * @return
     * @throws AgentArgumentException
     */
    private OID getAuthProtocol(String authMethod) throws AgentArgumentException {
        if (authMethod == null
                || authMethod.equalsIgnoreCase("none")
                || authMethod.length() == 0) {
            return null;
        } else if (authMethod.equalsIgnoreCase("md5")) {
            return AuthMD5.ID;
        } else if (authMethod.equalsIgnoreCase("sha")) {
            return AuthSHA.ID;
        } else {
            throw new AgentArgumentException("unknown authentication protocol: " + authMethod);
        }
    }

    public void close() throws IOException {
        logger.debug("关闭snmp连接{}",userInfo.getAddress());
        snmp.close();
    }

    public SNMPV3UserInfo getUserInfo() {
        return userInfo;
    }
}
