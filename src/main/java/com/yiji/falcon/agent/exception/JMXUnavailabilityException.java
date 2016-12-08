/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.exception;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-12-05 14:27 创建
 */

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guqiu@yiji.com
 */
public class JMXUnavailabilityException extends Exception {

    /**
     * JMX 连接不可用状态异常
     * @param e
     */
    public JMXUnavailabilityException(Exception e){
        super(e);
    }

    /**
     * JMX 连接不可用状态异常
     * @param msg
     */
    public JMXUnavailabilityException(String msg){
        super(msg);
    }

    /**
     * JMX 连接不可用状态异常
     * @param msg
     * @param e
     */
    public JMXUnavailabilityException(String msg,Exception e){
        super(msg,e);
    }
}
