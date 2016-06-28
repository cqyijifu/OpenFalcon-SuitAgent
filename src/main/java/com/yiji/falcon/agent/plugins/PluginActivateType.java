/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-24 11:18 创建
 */

/**
 * 插件运行活动类型
 * 定义插件以何种方式进行启动
 * @author guqiu@yiji.com
 */
public enum  PluginActivateType {
    /**
     * 插件不运行
     */
    DISABLED("不启用"),
    /**
     * 无论服务是否运行,都启动插件
     */
    FORCE("强制启动"),
    /**
     * 当监控服务可用时,自动进行插件启动
     */
    AUTO("自动运行");

    /**
     * 活动描述
     */
    private String desc;

    PluginActivateType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
