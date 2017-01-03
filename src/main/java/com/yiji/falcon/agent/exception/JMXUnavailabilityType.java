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
public enum JMXUnavailabilityType {
        /**
         * 连接失败
         */
        connectionFailed(0),
        /**
         * 获取JMX值超时
         */
        getMbeanValueTimeout(-1),
        /**
         * 获取JMX值发生异常
         */
        getMbeanValueException(-2),
        /**
         * 获取JMX对象集合超时
         */
        getObjectNameListTimeout(-3),
        /**
         * 获取JMX对象集合发生异常
         */
        getObjectNameListException(-4),
        /**
         * 未知异常
         */
        unKnown(-5),
        ;

        @Getter
        private int type;

        JMXUnavailabilityType(int type) {
            this.type = type;
        }
    }