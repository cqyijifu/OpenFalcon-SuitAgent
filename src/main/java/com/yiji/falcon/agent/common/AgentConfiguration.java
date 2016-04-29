package com.yiji.falcon.agent.common;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 系统配置
 * Created by QianLong on 16/4/25.
 */
public enum  AgentConfiguration {

    INSTANCE;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * quartz配置文件路径
     */
    private String QUARTZ_CONF_PATH = null;
    /**
     * log4j配置文件路径
     */
    private String LOG4J_CONF_PATH = null;
    /**
     * push到falcon的地址
     */
    private String AGENT_PUSH_URL = null;
    /**
     * Agent服务配置文件路径
     */
    private String AGENT_CONF_PATH = null;
    /**
     * agent监控指标的主体说明
     */
    private String AGENT_ENDPOINT = "";
    /**
     * ZK的数据采集周期
     */
    private int ZK_STEP = 60;
    /**
     * zk 的JMX 连接服务名
     */
    private String ZK_JMX_SERVER_NAME = null;
    /**
     * agent启动端口
     */
    private int AGENT_PORT = 4518;

    private static final String configName_agent_endpoint = "agent.endpoint";
    private static final String configName_zk_step = "agent.zk.metrics.step";
    private static final String configName_push_url = "agent.falcon.push.url";
    private static final String configName_zk_jmx_serverName = "agent.zk.jmx.serverName";
    private static final String configName_agent_port = "agent.port";

    /**
     * 初始化agent配置
     */
    AgentConfiguration() {
        if(StringUtils.isEmpty(System.getProperty("agent.conf.path"))){
            System.err.println("agent 配置文件位置读取失败,请确定系统配置项:" + "agent.conf.path");
            System.exit(0);
        }else{
            this.AGENT_CONF_PATH = System.getProperty("agent.conf.path");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.quartz.conf.path"))){
            System.err.println("quartz 配置文件位置读取失败,请确定系统配置项:" + "agent.quartz.conf.path");
            System.exit(0);
        }else{
            this.QUARTZ_CONF_PATH = System.getProperty("agent.quartz.conf.path");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.log4j.conf.path"))){
            System.err.println("log4j 配置文件位置读取失败,请确定系统配置项:" + "agent.log4j.conf.path");
            System.exit(0);
        }else{
            this.LOG4J_CONF_PATH = System.getProperty("agent.log4j.conf.path");
        }

        Properties agentConf = new Properties();
        try {
            agentConf.load(new FileInputStream(this.AGENT_CONF_PATH));
        } catch (IOException e) {
            log.error("{} 配置文件读取失败 Agent启动失败",this.AGENT_CONF_PATH);
            System.exit(0);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(configName_agent_port))){
            try {
                this.AGENT_PORT = Integer.parseInt(agentConf.getProperty(configName_agent_port));
            } catch (NumberFormatException e) {
                System.err.println("Agent启动失败,端口配置无效:" + agentConf.getProperty(configName_agent_port));
                System.exit(0);
            }
        }

        if(StringUtils.isEmpty(agentConf.getProperty(configName_push_url))){
            log.error("Agent启动失败,未定义falcon push地址配置:{}",configName_push_url);
            System.exit(0);
        }
        this.AGENT_PUSH_URL = agentConf.getProperty(configName_push_url);

        if(StringUtils.isEmpty(agentConf.getProperty(configName_zk_jmx_serverName))){
            log.error("Agent启动失败,未定义 zk 的 jmx 服务名配置:{}",configName_zk_jmx_serverName);
            System.exit(0);
        }
        this.ZK_JMX_SERVER_NAME = agentConf.getProperty(configName_zk_jmx_serverName);

        if(!StringUtils.isEmpty(agentConf.getProperty(configName_zk_step))){
            try {
                this.ZK_STEP = Integer.parseInt(agentConf.getProperty(configName_zk_step));
                if(this.ZK_STEP >= 24 * 60 * 60){
                    log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.AGENT_CONF_PATH,configName_zk_step);
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.AGENT_CONF_PATH,configName_zk_step);
                System.exit(0);
            }
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(configName_agent_endpoint))){
            this.AGENT_ENDPOINT = agentConf.getProperty(configName_agent_endpoint);
        }

    }

    public String getQUARTZ_CONF_PATH() {
        return QUARTZ_CONF_PATH;
    }

    public String getLOG4J_CONF_PATH() {
        return LOG4J_CONF_PATH;
    }

    public int getAGENT_PORT() {
        return AGENT_PORT;
    }

    public String getZK_JMX_SERVER_NAME(){
        return this.ZK_JMX_SERVER_NAME;
    }

    public String getAGENT_PUSH_URL(){
        return this.AGENT_PUSH_URL;
    }

    public String getAGENT_ENDPOINT(){
        return this.AGENT_ENDPOINT;
    }

    public String getAGENT_CONF_PATH(){
        return this.AGENT_CONF_PATH;
    }

    public int getZK_STEP(){
        return this.ZK_STEP;
    }
}
