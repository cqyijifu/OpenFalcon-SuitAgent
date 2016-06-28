/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-24 11:12 创建
 */

import com.yiji.falcon.agent.common.AgentJobHelper;
import com.yiji.falcon.agent.util.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author guqiu@yiji.com
 */
public class PluginExecute {

    private static final Logger logger = LoggerFactory.getLogger(PluginExecute.class);

    /**
     * 启动插件
     * @throws SchedulerException
     */
    public static void start() throws SchedulerException {
        //根据配置启动自发现功能
        AgentJobHelper.agentFlush();
        run();
    }

    /**
     * 运行插件
     */
    public static void run(){

        Set<Object> jmxPlugins = PluginLibraryHelper.getJMXPlugins();
        Set<Object> jdbcPlugins = PluginLibraryHelper.getJDBCPlugins();

        jmxPlugins.forEach(plugin -> {
            try {
                JMXPlugin jmxPlugin = (JMXPlugin) plugin;
                JobDataMap jobDataMap = new JobDataMap();
                //若jmxServerName有多个值,分别进行job启动
                for (String jmxServerName : ((JMXPlugin) plugin).jmxServerName().split(",")) {
                    if(!StringUtils.isEmpty(jmxServerName)){
                        String pluginName = String.format("%s-%s",jmxPlugin.pluginName(),jmxServerName);
                        jobDataMap.put("pluginName",pluginName);
                        jobDataMap.put("jmxServerName",jmxServerName);
                        jobDataMap.put("pluginObject",jmxPlugin);

                        AgentJobHelper.pluginWorkForJMX(pluginName,jmxPlugin.activateType(),jmxPlugin.step(),JMXPluginJob.class,pluginName,jmxPlugin.jmxServerName(),jmxPlugin.jmxServerName(),jobDataMap);
                    }
                }
            } catch (Exception e) {
                logger.error("插件启动异常",e);
            }
        });

        jdbcPlugins.forEach(plugin -> {
            try {
                JDBCPlugin jdbcPlugin = (JDBCPlugin) plugin;
                JobDataMap jobDataMap = new JobDataMap();
                String pluginName = String.format("%s-%s",jdbcPlugin.pluginName(),jdbcPlugin.serverName());
                jobDataMap.put("pluginObject",jdbcPlugin);
                AgentJobHelper.pluginWorkForJDBC(jdbcPlugin,pluginName,jdbcPlugin.activateType(),jdbcPlugin.step(),JDBCPluginJob.class,pluginName,jdbcPlugin.serverName(),jobDataMap);
            } catch (Exception e) {
                logger.error("插件启动异常",e);
            }
        });
    }

}
