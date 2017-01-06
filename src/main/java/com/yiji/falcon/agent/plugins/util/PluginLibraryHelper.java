/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-23 16:27 创建
 */

import com.google.common.reflect.ClassPath;
import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.plugins.*;
import com.yiji.falcon.agent.util.PropertiesUtil;
import com.yiji.falcon.agent.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件库
 * @author guqiu@yiji.com
 */
@Slf4j
public class PluginLibraryHelper {

    private final static Set<Plugin> plugins = new HashSet<>();
    private final static List<String> pluginNames = new ArrayList<>();

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    /**
     * 获取拥有授权配置的插件
     * @return
     */
    public static Set<Plugin> getPluginsAboutAuthorization(){
        Set<Plugin> targetPlugins = new HashSet<>();
        for (Plugin plugin : plugins) {
            if(hasAuthorizationConf(plugin.authorizationKeyPrefix())){
                targetPlugins.add(plugin);
            }
        }

        return targetPlugins;
    }

    /**
     * 是否有授权配置
     * @param authorizationKeyPrefix
     * 授权配置的字符串前缀
     * @return
     */
    private static boolean hasAuthorizationConf(String authorizationKeyPrefix){
        Map<String,String> authorizationConf = PropertiesUtil.getAllPropertiesByFileName(AgentConfiguration.INSTANCE.getAuthorizationConfPath());
        for (String key : authorizationConf.keySet()) {
            if(key != null && key.contains(authorizationKeyPrefix)){
                return true;
            }
        }
        return false;
    }

    /**
     * 根据配置文件名称查找匹配的插件
     * @param fileName
     * @return
     * 匹配不到返回null
     */
    public static Plugin getPluginByConfigFileName(String fileName){
        Plugin plugin = null;
        if(!StringUtils.isEmpty(fileName)){

            for (Plugin plugin1 : plugins) {
                if(fileName.equals(plugin1.configFileName())){
                    plugin = plugin1;
                    break;
                }
            }
        }
        return plugin;
    }

    /**
     * 调用所有注册的Plugin的agentShutdownHook方法
     */
    public static void invokeAgentShutdownHook(){
        plugins.forEach(Plugin::agentShutdownHook);
    }

    /**
     * 获取JMX监控服务插件
     * @return
     */
    public static Set<Plugin> getJMXPlugins(){
        return getPluginsByType(JMXPlugin.class);
    }

    /**
     * 获取JDBC监控服务插件
     * @return
     */
    public static Set<Plugin> getJDBCPlugins(){
        return getPluginsByType(JDBCPlugin.class);
    }

    /**
     * 获取SNMPV3监控服务插件
     * @return
     */
    public static Set<Plugin> getSNMPV3Plugins(){
        return getPluginsByType(SNMPV3Plugin.class);
    }

    /**
     * 获取探测监控服务插件
     * @return
     */
    public static Set<Plugin> getDetectPlugins(){
        return getPluginsByType(DetectPlugin.class);
    }

    private static Set<Plugin> getPluginsByType(Class type){
        Set<Plugin> targetPlugins = new HashSet<>();
        targetPlugins.addAll(plugins.stream().filter(plugin -> type.isAssignableFrom(plugin.getClass())).collect(Collectors.toSet()));
        return targetPlugins;
    }

    /**
     * 获取插件的配置
     * @param plugin
     * @return
     */
    public static  Map<String,String> getPluginConfig(Plugin plugin){
        Map<String,String> config = new HashMap<>();
        config.put("pluginDir",AgentConfiguration.INSTANCE.getPluginConfPath());
        if(!StringUtils.isEmpty(plugin.configFileName())){
            //指定了插件名,传入插件配置
            config.putAll(PropertiesUtil.getAllPropertiesByFileName(AgentConfiguration.INSTANCE.getPluginConfPath() + File.separator + plugin.configFileName()));
        }
        String authorizationKeyPrefix = plugin.authorizationKeyPrefix();
        if(!StringUtils.isEmpty(authorizationKeyPrefix)){
            //传入授权配置
            Map<String,String> authorizationConf = PropertiesUtil.getAllPropertiesByFileName(AgentConfiguration.INSTANCE.getAuthorizationConfPath());
            authorizationConf.entrySet().stream().filter(entry -> entry.getKey() != null &&
                    entry.getKey().contains(authorizationKeyPrefix)).forEach(entry -> {
                config.put(entry.getKey(), entry.getValue());
            });
        }
        return config;
    }

    /**
     * 注册插件
     */
    public synchronized void register(){
        Set<ClassPath.ClassInfo> classInfos;
        try {
            classInfos = ClassPath.from(classLoader).getTopLevelClassesRecursive("com.yiji.falcon");
            for (ClassPath.ClassInfo classInfo : classInfos) {
                String clazzName = classInfo.getName();
                Class clazz = Class.forName(clazzName);
                if(Plugin.class.isAssignableFrom(clazz) &&
                        clazz != Plugin.class &&
                        clazz != JMXPlugin.class &&
                        clazz != JDBCPlugin.class &&
                        clazz != SNMPV3Plugin.class &&
                        clazz != DetectPlugin.class){
                    final Plugin plugin = (Plugin) clazz.newInstance();
                    //插件初始化操作
                    Map<String,String> config = getPluginConfig(plugin);
                    //初始化插件配置
                    plugin.init(config);

                    String avaStr = pluginAvailable(plugin);
                    if(avaStr != null){
                        log.warn("插件 {} 无效 : {}",clazz.getName(),avaStr);
                        continue;
                    }
                    plugins.add(plugin);
                    log.info("成功注册插件:{},启动方式:{}",clazzName,plugin.activateType().getDesc());
                }
            }
        } catch (Exception e) {
            log.error("插件注册异常",e);
        }
    }

    /**
     * 插件有效性判断
     * @return
     */
    private String pluginAvailable(Plugin plugin){
        try {
            int step = plugin.step();
            int stepMin = 30;
            int stepMax = 24 * 60 * 60;
            if(step < stepMin || step >= stepMax){
                return String.format("step不能小于%d 并且不能大于等于%d",stepMin,stepMax);
            }

            String pluginName = plugin.pluginName();
            if(pluginNames.contains(pluginName)){
                return String.format("插件 {%s} 的插件名称 {%s} 重复,请重新执行插件名称",plugin.getClass().getName(),pluginName);
            }
            pluginNames.add(pluginName);

            if(StringUtils.isEmpty(plugin.serverName())){
                return String.format("插件 {%s} 的serverName不能为空",plugin.getClass().getName());
            }
        } catch (Exception e) {
            log.warn("插件无效",e);
            return null;
        }

        return null;
    }

}
