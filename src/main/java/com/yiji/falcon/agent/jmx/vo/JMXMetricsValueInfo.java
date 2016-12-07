/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx.vo;

import lombok.Data;

import java.util.List;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * JMX mBean值的info类
 * @author guqiu@yiji.com
 */
@Data
public class JMXMetricsValueInfo {

    /**
     * 此jmx 连接的对象信息
     */
    private List<JMXObjectNameInfo> jmxObjectNameInfoList;

    /**
     * jmx 连接信息
     */
    private JMXConnectionInfo jmxConnectionInfo;

}
