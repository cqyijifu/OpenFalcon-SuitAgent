/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo.snmp;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-12 16:01 创建
 */

import lombok.Data;

import java.util.Date;

/**
 * @author guqiu@yiji.com
 */
@Data
public class IfStatVO {
    private String ifName;
    private int ifIndex;
    private String ifHCInOctets;
    private String ifHCOutOctets;
    private String ifHCInUcastPkts;
    private String ifHCOutUcastPkts;
    private String ifHCInBroadcastPkts;
    private String ifHCOutBroadcastPkts;
    private String ifHCInMulticastPkts;
    private String ifHCOutMulticastPkts;
    private String ifOperStatus;
    private Date time;

}
