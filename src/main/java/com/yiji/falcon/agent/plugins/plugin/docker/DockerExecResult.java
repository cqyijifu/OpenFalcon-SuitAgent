/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.docker;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-10 15:53 创建
 */

/**
 * Docker执行exec命令返回值封装类
 * @author guqiu@yiji.com
 */
class DockerExecResult {
    /**
     * 执行是否成功
     */
    private boolean isSuccess;
    /**
     * 执行返回的结果
     */
    private String result;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "DockerExecResult{" +
                "isSuccess=" + isSuccess +
                ", result='" + result + '\'' +
                '}';
    }
}
