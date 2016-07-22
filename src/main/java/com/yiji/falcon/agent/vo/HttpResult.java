/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 15:39 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class HttpResult {

    /**
     * 状态码
     */
    private int status;
    /**
     * 返回结果
     */
    private String result;

    @Override
    public String toString() {
        return "HttpResult{" +
                "status='" + status + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
