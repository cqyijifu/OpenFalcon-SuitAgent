/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.vo;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 15:39 创建
 */

import lombok.Data;

/**
 * @author guqiu@yiji.com
 */
@Data
public class HttpResult {

    /**
     * 状态码
     */
    private int status;
    /**
     * 返回结果
     */
    private String result;

    /**
     * 响应时间
     */
    private long responseTime;
}
