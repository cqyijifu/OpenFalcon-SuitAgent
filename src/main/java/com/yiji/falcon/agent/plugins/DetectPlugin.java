/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-21 14:57 创建
 */

import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.vo.detect.DetectResult;

import java.util.Collection;

/**
 * 探测监控插件
 * @author guqiu@yiji.com
 */
public interface DetectPlugin extends Plugin {

    /**
     * 监控的具体服务的agentSignName tag值
     * @param address
     * 被监控的探测地址
     * @return
     * 根据地址提炼的标识,如域名等
     */
    String agentSignName(String address);

    /**
     * 一次地址的探测结果
     * @param address
     * 被探测的地址,地址来源于方法 {@link com.yiji.falcon.agent.plugins.DetectPlugin#detectAddressCollection()}
     * @return
     * 返回被探测的地址的探测结果,将用于上报监控状态
     */
    DetectResult detectResult(String address);

    /**
     * 被探测的地址集合
     * @return
     * 只要该集合不为空,就会触发监控
     * pluginActivateType属性将不起作用
     */
    Collection<String> detectAddressCollection();

    /**
     * 无需配置该属性,此类插件pluginActivateType属性将不起作用
     * 运行方式见 {@link com.yiji.falcon.agent.plugins.DetectPlugin#detectAddressCollection()}
     *
     * 插件运行方式
     *
     * @return
     */
    @Override
    default PluginActivateType activateType(){
        return PluginActivateType.AUTO;
    }
}
