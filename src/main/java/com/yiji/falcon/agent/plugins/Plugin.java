/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-23 16:56 创建
 */

import com.yiji.falcon.agent.plugins.util.PluginActivateType;

import java.util.Map;

/**
 * @author guqiu@yiji.com
 */
public interface Plugin {

    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     * @param properties
     * 包含的配置:
     * 1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     * 2、插件指定的配置文件的全部配置信息(参见 {@link com.yiji.falcon.agent.plugins.Plugin#configFileName()} 接口项)
     * 3、授权配置项(参见 {@link com.yiji.falcon.agent.plugins.Plugin#authorizationKeyPrefix()} 接口项
     */
    void init(Map<String,String> properties);

    /**
     * 插件名
     * 默认为插件的简单类名
     * @return
     */
    default String pluginName(){
        return this.getClass().getSimpleName();
    }

    /**
     * 该插件在指定插件配置目录下的配置文件名
     * @return
     * 返回该插件对应的配置文件名
     * 默认值:插件简单类名第一个字母小写 加 .properties 后缀
     */
    default String configFileName(){
        String className = this.getClass().getSimpleName();
        return className.substring(0,1).toLowerCase() + className.substring(1) + ".properties";
    }

    /**
     * 授权登陆配置的key前缀(配置在authorization.properties文件中)
     * 将会通过init方法的map属性中,将符合该插件的授权配置传入,以供插件进行初始化操作
     *
     * 如 authorizationKeyPrefix = authorization.prefix , 并且在配置文件中配置了如下信息:
     * authorization.prefix.xxx1 = xxx1
     * authorization.prefix.xxx2 = xxx2
     * 则init中的map中将会传入该KV:
     * authorization.prefix.xxx1 : xxx1
     * authorization.prefix.xxx2 : xxx2
     *
     * @return
     * 若不覆盖此方法,默认返回空,既该插件无需授权配置
     */
    default String authorizationKeyPrefix(){
        return "";
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     * @return
     */
    String serverName();

    /**
     * 监控值的获取和上报周期(秒)
     * @return
     */
    int step();

    /**
     * 插件运行方式
     * @return
     */
    PluginActivateType activateType();

    /**
     * Agent关闭时的调用钩子
     * 如，可用于插件的资源释放等操作
     */
    void agentShutdownHook();

}
