/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.exception;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-12-05 14:27 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class JMXConnectUnavailabilityException extends Exception {

    /**
     * JMX 连接不可用状态异常
     * @param e
     */
    public JMXConnectUnavailabilityException(Exception e){
        super(e);
    }
}
