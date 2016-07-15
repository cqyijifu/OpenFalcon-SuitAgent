/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo.snmp;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-12 16:01 创建
 */

import java.util.Date;

/**
 * @author guqiu@yiji.com
 */
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

    @Override
    public String toString() {
        return "IfStatVO{" +
                "ifName='" + ifName + '\'' +
                ", ifIndex='" + ifIndex + '\'' +
                ", ifHCInOctets='" + ifHCInOctets + '\'' +
                ", ifHCOutOctets='" + ifHCOutOctets + '\'' +
                ", ifHCInUcastPkts='" + ifHCInUcastPkts + '\'' +
                ", ifHCOutUcastPkts='" + ifHCOutUcastPkts + '\'' +
                ", ifHCInBroadcastPkts='" + ifHCInBroadcastPkts + '\'' +
                ", ifHCOutBroadcastPkts='" + ifHCOutBroadcastPkts + '\'' +
                ", ifHCInMulticastPkts='" + ifHCInMulticastPkts + '\'' +
                ", ifHCOutMulticastPkts='" + ifHCOutMulticastPkts + '\'' +
                ", ifOperStatus='" + ifOperStatus + '\'' +
                ", time=" + time +
                '}';
    }

    public int getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(int ifIndex) {
        this.ifIndex = ifIndex;
    }

    public String getIfName() {
        return ifName;
    }

    public void setIfName(String ifName) {
        this.ifName = ifName;
    }

    public String getIfHCInOctets() {
        return ifHCInOctets;
    }

    public void setIfHCInOctets(String ifHCInOctets) {
        this.ifHCInOctets = ifHCInOctets;
    }

    public String getIfHCOutOctets() {
        return ifHCOutOctets;
    }

    public void setIfHCOutOctets(String ifHCOutOctets) {
        this.ifHCOutOctets = ifHCOutOctets;
    }

    public String getIfHCInUcastPkts() {
        return ifHCInUcastPkts;
    }

    public void setIfHCInUcastPkts(String ifHCInUcastPkts) {
        this.ifHCInUcastPkts = ifHCInUcastPkts;
    }

    public String getIfHCOutUcastPkts() {
        return ifHCOutUcastPkts;
    }

    public void setIfHCOutUcastPkts(String ifHCOutUcastPkts) {
        this.ifHCOutUcastPkts = ifHCOutUcastPkts;
    }

    public String getIfHCInBroadcastPkts() {
        return ifHCInBroadcastPkts;
    }

    public void setIfHCInBroadcastPkts(String ifHCInBroadcastPkts) {
        this.ifHCInBroadcastPkts = ifHCInBroadcastPkts;
    }

    public String getIfHCOutBroadcastPkts() {
        return ifHCOutBroadcastPkts;
    }

    public void setIfHCOutBroadcastPkts(String ifHCOutBroadcastPkts) {
        this.ifHCOutBroadcastPkts = ifHCOutBroadcastPkts;
    }

    public String getIfHCInMulticastPkts() {
        return ifHCInMulticastPkts;
    }

    public void setIfHCInMulticastPkts(String ifHCInMulticastPkts) {
        this.ifHCInMulticastPkts = ifHCInMulticastPkts;
    }

    public String getIfHCOutMulticastPkts() {
        return ifHCOutMulticastPkts;
    }

    public void setIfHCOutMulticastPkts(String ifHCOutMulticastPkts) {
        this.ifHCOutMulticastPkts = ifHCOutMulticastPkts;
    }

    public String getIfOperStatus() {
        return ifOperStatus;
    }

    public void setIfOperStatus(String ifOperStatus) {
        this.ifOperStatus = ifOperStatus;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
