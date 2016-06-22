/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.falcon;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 计数类型
 * @author guqiu@yiji.com
 */
public enum CounterType {
    /**
     * 用户上传什么样的值，就原封不动的存储
     */
    GAUGE,
    /**
     * 指标在存储和展现的时候，会被计算为speed，即（当前值 - 上次值）/ 时间间隔
     */
    COUNTER
}