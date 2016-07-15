/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.exception;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-12 11:06 创建
 */

/**
 * 因Agent的配置参数、插件参数等导致的异常
 * @author guqiu@yiji.com
 */
public class AgentArgumentException extends Exception {

    private String err;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param err the detail message. The detail message is saved for
     *            later retrieval by the {@link #getMessage()} method.
     */
    public AgentArgumentException(String err) {
        super(err);
        this.err = err;
    }

    public String getErr() {
        return err;
    }

}