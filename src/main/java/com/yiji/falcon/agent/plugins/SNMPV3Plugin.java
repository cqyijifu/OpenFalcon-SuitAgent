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
import java.util.List;

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
     * @param sessions
     * 连接设备的SNMP Session,插件可通过此对象进行设备间的SNMP通信,以获取监控数据
     * @return
     */
    Collection<FalconReportObject> inbuiltReportObjectsForValid(List<SNMPV3Session> sessions);
}
