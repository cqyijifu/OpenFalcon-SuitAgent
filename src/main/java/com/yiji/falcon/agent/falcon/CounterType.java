package com.yiji.falcon.agent.falcon;

/**
 * 计数类型
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