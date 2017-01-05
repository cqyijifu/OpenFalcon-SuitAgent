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

import java.util.List;

/**
 * @author guqiu@yiji.com
 */
public class JMXUnavailabilityException extends Exception {

    @Getter
    private JMXUnavailabilityType type;

    @Getter
    private List<JMXUnavailabilityException> exceptions;

    @Override
    public String toString() {
        return "JMXUnavailabilityException{" +
                "type=" + type +
                "msg=" + getMessage() +
                '}';
    }

    /**
     * mBean值获取超时的异常集合
     * @param exceptions
     */
    public JMXUnavailabilityException(List<JMXUnavailabilityException> exceptions) {
        this.exceptions = exceptions;
        this.type = JMXUnavailabilityType.getMbeanValueTimeout;
    }

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
