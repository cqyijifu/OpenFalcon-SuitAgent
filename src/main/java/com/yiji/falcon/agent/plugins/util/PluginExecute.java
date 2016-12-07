/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-24 11:12 创建
 */

import com.yiji.falcon.agent.common.AgentJobHelper;
import com.yiji.falcon.agent.plugins.*;
import com.yiji.falcon.agent.plugins.job.DetectPluginJob;
import com.yiji.falcon.agent.plugins.job.JDBCPluginJob;
import com.yiji.falcon.agent.plugins.job.SNMPPluginJob;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.snmp.SNMPV3UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class PluginExecute {

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

        Set<Plugin> jmxPlugins = PluginLibraryHelper.getJMXPlugins();
        Set<Plugin> jdbcPlugins = PluginLibraryHelper.getJDBCPlugins();
        Set<Plugin> snmpv3Plugins = PluginLibraryHelper.getSNMPV3Plugins();
        Set<Plugin> detectPlugins = PluginLibraryHelper.getDetectPlugins();

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

                        AgentJobHelper.pluginWorkForJMX(pluginName,
                                jmxPlugin.activateType(),
                                jmxPlugin.step(),
                                pluginName,
                                jmxPlugin.serverName() + "-" + jmxServerName,
                                jmxServerName,
                                jobDataMap);
                    }
                }
            } catch (Exception e) {
                log.error("插件启动异常",e);
            }
        });
        snmpv3Plugins.forEach(plugin -> {
            try{
                SNMPV3Plugin snmpv3Plugin = (SNMPV3Plugin) plugin;
                JobDataMap jobDataMap = new JobDataMap();
                String pluginName = String.format("%s-%s",snmpv3Plugin.pluginName(),snmpv3Plugin.serverName());
                jobDataMap.put("pluginName",pluginName);
                jobDataMap.put("pluginObject",snmpv3Plugin);
                Collection<SNMPV3UserInfo> userInfoCollection = snmpv3Plugin.userInfo();
                List<SNMPV3UserInfo> jobUsers = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                int count = 1;
                for (SNMPV3UserInfo snmpv3UserInfo : userInfoCollection) {
                    //每5个SNMP连接为一个job
                    if(jobUsers.size() == 5){
                        jobDataMap.put("userInfoList",jobUsers);
                        AgentJobHelper.pluginWorkForSNMPV3(snmpv3Plugin,pluginName,SNMPPluginJob.class,pluginName + "-" + count + "-" + sb.toString(),snmpv3Plugin.serverName() + "-" + count + "-" + sb.toString(),jobDataMap);
                        jobUsers = new ArrayList<>();
                        jobUsers.add(snmpv3UserInfo);
                        sb = new StringBuilder();
                        sb.append(snmpv3UserInfo.getAddress()).append(" | ");
                        count++;
                    }else{
                        sb.append(snmpv3UserInfo.getAddress()).append(" | ");
                        jobUsers.add(snmpv3UserInfo);
                    }
                }
                AgentJobHelper.pluginWorkForSNMPV3(snmpv3Plugin,pluginName,SNMPPluginJob.class,pluginName + "-" + count + "-" + sb.toString(),snmpv3Plugin.serverName() + "-" + count + "-" + sb.toString(),jobDataMap);
            }catch (Exception e){
                log.error("插件启动异常",e);
            }
        });

        detectPlugins.forEach(plugin -> {
            try {
                DetectPlugin detectPlugin = (DetectPlugin) plugin;
                JobDataMap jobDataMap = new JobDataMap();
                String pluginName = plugin.pluginName();
                jobDataMap.put("pluginName",pluginName);
                jobDataMap.put("pluginObject",detectPlugin);
                AgentJobHelper.pluginWorkForDetect(detectPlugin,pluginName, DetectPluginJob.class,jobDataMap);
            }catch (Exception e){
                log.error("插件启动异常",e);
            }
        });

        //设置JDBC超时为5秒
        DriverManager.setLoginTimeout(5);
        jdbcPlugins.forEach(plugin -> {
            try {
                JDBCPlugin jdbcPlugin = (JDBCPlugin) plugin;
                JobDataMap jobDataMap = new JobDataMap();
                String pluginName = String.format("%s-%s",jdbcPlugin.pluginName(),jdbcPlugin.serverName());
                jobDataMap.put("pluginName",pluginName);
                jobDataMap.put("pluginObject",jdbcPlugin);
                AgentJobHelper.pluginWorkForJDBC(jdbcPlugin,pluginName,JDBCPluginJob.class,pluginName,jdbcPlugin.serverName(),jobDataMap);
            } catch (Exception e) {
                log.error("插件启动异常",e);
            }
        });

    }

}
