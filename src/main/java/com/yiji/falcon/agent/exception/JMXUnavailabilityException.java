/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.exception;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-12-05 14:27 创建
 */

import lombok.Getter;

/**
 * @author guqiu@yiji.com
 */
public class JMXUnavailabilityException extends Exception {

    @Getter
    private JMXUnavailabilityType type;

    /**
     * JMX 连接不可用状态异常
     * @param type
     * @param e
     */
    public JMXUnavailabilityException(JMXUnavailabilityType type, Exception e){
        super(e);
        this.type = type;
    }

    /**
     * JMX 连接不可用状态异常
     * @param type
     * @param msg
     */
    public JMXUnavailabilityException(JMXUnavailabilityType type, String msg){
        super(msg);
        this.type = type;
    }

    /**
     * JMX 连接不可用状态异常
     * @param type
     * @param msg
     * @param e
     */
    public JMXUnavailabilityException(JMXUnavailabilityType type, String msg, Exception e){
        super(msg,e);
    }
}
