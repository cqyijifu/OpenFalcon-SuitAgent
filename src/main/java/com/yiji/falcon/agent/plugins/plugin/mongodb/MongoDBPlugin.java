/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.mongodb;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-10-25 15:09 创建
 */

import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guqiu@yiji.com
 */
public class MongoDBPlugin implements DetectPlugin {
    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     *
     * @param properties 包含的配置:
     *                   1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     *                   2、插件指定的配置文件的全部配置信息(参见 {@link Plugin#configFileName()} 接口项)
     *                   3、授权配置项(参见 {@link Plugin#authorizationKeyPrefix()} 接口项
     */
    @Override
    public void init(Map<String, String> properties) {

    }

    /**
     * 该插件在指定插件配置目录下的配置文件名
     *
     * @return 返回该插件对应的配置文件名
     * 默认值:插件简单类名第一个字母小写 加 .properties 后缀
     */
    @Override
    public String configFileName() {
        return "mongoDBPlugin.properties";
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return null;
    }

    /**
     * 监控值的获取和上报周期(秒)
     *
     * @return
     */
    @Override
    public int step() {
        return 0;
    }

    /**
     * Agent关闭时的调用钩子
     * 如，可用于插件的资源释放等操作
     */
    @Override
    public void agentShutdownHook() {

    }

    /**
     * 监控的具体服务的agentSignName tag值
     *
     * @param address 被监控的探测地址
     * @return 根据地址提炼的标识, 如域名等
     */
    @Override
    public String agentSignName(String address) {
        return null;
    }

    /**
     * 一次地址的探测结果
     *
     * @param address 被探测的地址,地址来源于方法 {@link DetectPlugin#detectAddressCollection()}
     * @return 返回被探测的地址的探测结果, 将用于上报监控状态
     */
    @Override
    public DetectResult detectResult(String address) {
        return null;
    }

    /**
     * 被探测的地址集合
     *
     * @return 只要该集合不为空, 就会触发监控
     * pluginActivateType属性将不起作用
     */
    @Override
    public Collection<String> detectAddressCollection() {
        return null;
    }

    /**
     * 自动探测地址的实现
     * 若配置文件已配置地址,将不会调用此方法
     * 若配置文件未配置探测地址的情况下,将会调用此方法,若该方法返回非null且有元素的集合,则启动运行插件,使用该方法返回的探测地址进行监控
     *
     * @return
     */
    @Override
    public Collection<String> autoDetectAddress() {
        try {
            CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(
                    "whereis mongo",false,10, TimeUnit.SECONDS
            );
            if(!StringUtils.isEmpty(executeResult.msg)){
                String msg = executeResult.msg;
                String[] ss = msg.split("\\s+");
                for (String s : ss) {
                    if(s.startsWith("/")){
                        //返回mongo的命令地址作为启动标志
                        return Collections.singletonList(s);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
