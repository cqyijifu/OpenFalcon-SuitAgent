/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-01 17:19 创建
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.plugins.util.SNMPV3Session;
import com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo;

import java.util.Collection;
import java.util.Map;

/**
 * SNMP V3协议的监控插件
 * @author guqiu@yiji.com
 */
public interface SNMPV3Plugin extends Plugin{

    /**
     * 通过SNMPV3协议获取设备监控信息的登陆用户信息
     * @return
     * list
     * 一个 {@link com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo} 对象就是一个设备的监控
     */
    Collection<SNMPV3UserInfo> userInfo();

    /**
     * 当设备链接可用时的插件内置报告,如该插件适配的不同设备和品牌的SNMP监控报告
     * Agent会自动采集一些公共的MIB数据,但是设备私有的MIB信息,将由不同的插件自己提供
     * @param session
     * 连接设备的SNMP Session,插件可通过此对象进行设备间的SNMP通信,以获取监控数据
     * @return
     */
    Collection<FalconReportObject> inbuiltReportObjectsForValid(SNMPV3Session session);

    /**
     * 是否需要进行接口数据采集
     * @return
     */
    boolean hasIfCollect();

    /**
     * 允许采集哪些接口数据，用map的形式返回，key为接口metrics名称，value为是否允许。
     * 若对应key的metrics获取为null或false，均不采集。key的集合为：
     *
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
    Map<String,Boolean> ifCollectMetricsEnable();

}
