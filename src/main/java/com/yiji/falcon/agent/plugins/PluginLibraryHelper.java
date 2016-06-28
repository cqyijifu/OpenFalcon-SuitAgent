/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-23 16:27 创建
 */

import com.google.common.reflect.ClassPath;
import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.util.PropertiesUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件库
 * @author guqiu@yiji.com
 */
public class PluginLibraryHelper {

    private final static Set<Object> plugins = new HashSet<>();
    private final static List<String> pluginNames = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ClassLoader classLoader = this.getClass().getClassLoader();

    /**
     * 获取JMX监控服务插件
     * @return
     */
    public static Set<Object> getJMXPlugins(){
        Set<Object> targetPlugins = new HashSet<>();
        targetPlugins.addAll(plugins.stream().filter(plugin -> JMXPlugin.class.isAssignableFrom(plugin.getClass())).collect(Collectors.toSet()));
        return targetPlugins;
    }

    /**
     * 获取JDBC监控服务插件
     * @return
     */
    public static Set<Object> getJDBCPlugins(){
        Set<Object> targetPlugins = new HashSet<>();
        targetPlugins.addAll(plugins.stream().filter(plugin -> JDBCPlugin.class.isAssignableFrom(plugin.getClass())).collect(Collectors.toSet()));
        return targetPlugins;
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
                        clazz != JDBCPlugin.class){
                    Plugin plugin = (Plugin) clazz.newInstance();
                    //插件初始化操作
                    Map<String,String> config = new HashMap<>();
                    config.put("pluginDir",AgentConfiguration.INSTANCE.getPluginConfPath());
                    if(StringUtils.isEmpty(plugin.configFileName())){
                        plugin.init(config);
                    }else{
                        config.putAll(PropertiesUtil.getAllPropertiesByFileName(AgentConfiguration.INSTANCE.getPluginConfPath() + File.separator + plugin.configFileName()));
                        plugin.init(config);
                    }
                    String avaStr = pluginAvailable(plugin);
                    if(avaStr != null){
                        logger.warn("插件 {} 无效 : {}",clazz.getName(),avaStr);
                        continue;
                    }
                    plugins.add(plugin);
                    logger.info("成功注册插件:{}",clazzName);
                }
            }
        } catch (Exception e) {
            logger.error("插件注册异常",e);
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
            logger.warn("插件无效",e);
            return null;
        }

        return null;
    }

}
