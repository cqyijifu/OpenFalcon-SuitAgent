/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.config;

import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 系统配置
 * @author guqiu@yiji.com
 */
public enum  AgentConfiguration {

    INSTANCE;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * quartz配置文件路径
     */
    private String quartzConfPath = null;
    /**
     * log4j配置文件路径
     */
    private String log4JConfPath = null;
    /**
     * push到falcon的地址
     */
    private String agentPushUrl = null;

    /**
     * 插件配置的目录
     */
    private String pluginConfPath;

    /**
     * 服务配置文件路径
     */
    private String agentConfPath = null;
    private String jmxCommonMetricsConfPath = null;

    /**
     * agent监控指标的主体说明
     */
    private String agentEndpoint = "";

    /**
     * agent启动端口
     */
    private int agentPort = 4518;

    /**
     * agent自动发现服务刷新周期
     */
    private int agentFlushTime = 300;


    private static final String CONF_AGENT_ENDPOINT = "agent.endpoint";
    private static final String CONF_AGENT_FLUSH_TIME = "agent.flush.time";

    private static final String CONF_AGENT_FALCON_PUSH_URL = "agent.falcon.push.url";
    private static final String CONF_AGENT_PORT = "agent.port";

    private Properties agentConf = null;

    /**
     * 初始化agent配置
     */
    AgentConfiguration() {
        if(StringUtils.isEmpty(System.getProperty("agent.conf.path"))){
            log.error("agent 配置文件位置读取失败,请确定系统配置项:" + "agent.conf.path");
            System.exit(0);
        }else{
            this.agentConfPath = System.getProperty("agent.conf.path");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.plugin.conf.dir"))){
            log.error("agent 配置文件位置读取失败,请确定系统配置项:" + "agent.plugin.conf.dir");
            System.exit(0);
        }else{
            this.pluginConfPath = System.getProperty("agent.plugin.conf.dir");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.quartz.conf.path"))){
            log.error("quartz 配置文件位置读取失败,请确定系统配置项:" + "agent.quartz.conf.path");
            System.exit(0);
        }else{
            this.quartzConfPath = System.getProperty("agent.quartz.conf.path");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.log4j.conf.path"))){
            log.error("log4j 配置文件位置读取失败,请确定系统配置项:" + "agent.log4j.conf.path");
            System.exit(0);
        }else{
            this.log4JConfPath = System.getProperty("agent.log4j.conf.path");
        }

        agentConf = new Properties();
        try(FileInputStream in = new FileInputStream(this.agentConfPath)){
            agentConf.load(in);
        }catch (IOException e) {
            log.error("{} 配置文件读取失败 Agent启动失败",this.agentConfPath);
            System.exit(0);
        }
        init();
        initJMXCommon();
    }

    private void init(){

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ENDPOINT))){
            this.agentEndpoint = agentConf.getProperty(CONF_AGENT_ENDPOINT);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_PORT))){
            try {
                this.agentPort = Integer.parseInt(agentConf.getProperty(CONF_AGENT_PORT));
            } catch (NumberFormatException e) {
                log.error("Agent启动失败,端口配置{}无效:{}", CONF_AGENT_PORT, agentConf.getProperty(CONF_AGENT_PORT));
                System.exit(0);
            }
        }

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_FALCON_PUSH_URL))){
            log.error("Agent启动失败,未定义falcon push地址配置:{}", CONF_AGENT_FALCON_PUSH_URL);
            System.exit(0);
        }
        this.agentPushUrl = agentConf.getProperty(CONF_AGENT_FALCON_PUSH_URL);

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_FLUSH_TIME))){
            log.error("Agent启动失败,未指定配置:{}", CONF_AGENT_FLUSH_TIME);
            System.exit(0);
        }
        try {
            this.agentFlushTime = Integer.parseInt(agentConf.getProperty(CONF_AGENT_FLUSH_TIME));
        } catch (NumberFormatException e) {
            log.error("Agent启动失败,自动发现服务的刷新周期配置{}无效:{}",CONF_AGENT_FLUSH_TIME,agentConf.getProperty(CONF_AGENT_FLUSH_TIME));
            System.exit(0);
        }

    }

    private void initJMXCommon(){
        String property = System.getProperty("agent.jmx.metrics.common.path");
        if(StringUtils.isEmpty(property)){
            log.error("jmx 的公共配置系统属性文件未定义:agent.jmx.metrics.common.path");
            System.exit(0);
        }
        this.jmxCommonMetricsConfPath = property;
    }

    public String getQuartzConfPath() {
        return quartzConfPath;
    }

    public String getLog4JConfPath() {
        return log4JConfPath;
    }

    public int getAgentPort() {
        return agentPort;
    }

    public String getAgentPushUrl(){
        return this.agentPushUrl;
    }

    public String getAgentEndpoint(){
        return this.agentEndpoint;
    }

    public String getAgentConfPath(){
        return this.agentConfPath;
    }

    public String getJmxCommonMetricsConfPath() {
        return jmxCommonMetricsConfPath;
    }

    public int getAgentFlushTime() {
        return agentFlushTime;
    }

    public String getPluginConfPath() {
        return pluginConfPath;
    }
}
