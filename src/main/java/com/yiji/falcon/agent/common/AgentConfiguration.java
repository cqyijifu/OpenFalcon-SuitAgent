package com.yiji.falcon.agent.common;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
     * Agent服务配置文件路径
     */
    private String agentConfPath = null;
    /**
     * agent监控指标的主体说明
     */
    private String agentEndpoint = "";
    /**
     * ZK的数据采集周期
     */
    private int zkStep = 60;
    private int tomcatStep = 60;
    private int oracleStep = 60;

    /**
     * zk 的JMX 连接服务名
     */
    private String zkJmxServerName = null;
    private String tomcatJmxServerName = null;
    /**
     * agent启动端口
     */
    private int agentPort = 4518;

    //服务开启项
    private boolean agentZkWork = false;
    private boolean agentTomcatWork = false;
    private boolean agentOracleWork = false;

    //Oracle
    private String oracleJDBCDriver;
    private String oracleJDBCUrl;
    private String oracleJDBCUsername;
    private String oracleJDBCPassword;
    private final Map<String,String> oracleGenericQueries = new HashMap<>();

    private static final String CONF_AGENT_ENDPOINT = "agent.endpoint";

    private static final String CONF_AGENT_ZK_METRICS_STEP = "agent.zk.metrics.step";
    private static final String CONF_AGENT_TOMCAT_METRICS_STEP = "agent.tomcat.metrics.step";
    private static final String CONF_AGENT_ORACLE_METRICS_STEP = "agent.oracle.metrics.step";

    private static final String CONF_AGENT_FALCON_PUSH_URL = "agent.falcon.push.url";
    private static final String CONF_AGENT_ZK_JMX_SERVER_NAME = "agent.zk.jmx.serverName";
    private static final String CONF_AGENT_TOMCAT_JMX_SERVER_NAME = "agent.tomcat.jmx.serverName";
    private static final String CONF_AGENT_PORT = "agent.port";

    private static final String CONF_AGENT_ZK_WORK = "agent.zk.work";
    private static final String CONF_AGENT_ORACLE_WORK = "agent.oracle.work";
    private static final String CONF_AGENT_TOMCAT_WORK = "agent.tomcat.work";

    private static final String CONF_AGENT_ORACLE_JDBC_DRIVER = "agent.oracle.jdbc.driver";
    private static final String CONF_AGENT_ORACLE_JDBC_URL = "agent.oracle.jdbc.url";
    private static final String CONF_AGENT_ORACLE_JDBC_USERNAME = "agent.oracle.jdbc.username";
    private static final String CONF_AGENT_ORACLE_JDBC_PSWD = "agent.oracle.jdbc.password";

    private Properties agentConf = null;

    /**
     * 初始化agent配置
     */
    AgentConfiguration() {
        if(StringUtils.isEmpty(System.getProperty("agent.conf.path"))){
            System.err.println("agent 配置文件位置读取失败,请确定系统配置项:" + "agent.conf.path");
            System.exit(0);
        }else{
            this.agentConfPath = System.getProperty("agent.conf.path");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.quartz.conf.path"))){
            System.err.println("quartz 配置文件位置读取失败,请确定系统配置项:" + "agent.quartz.conf.path");
            System.exit(0);
        }else{
            this.quartzConfPath = System.getProperty("agent.quartz.conf.path");
        }

        if(StringUtils.isEmpty(System.getProperty("agent.log4j.conf.path"))){
            System.err.println("log4j 配置文件位置读取失败,请确定系统配置项:" + "agent.log4j.conf.path");
            System.exit(0);
        }else{
            this.log4JConfPath = System.getProperty("agent.log4j.conf.path");
        }

        agentConf = new Properties();
        try {
            agentConf.load(new FileInputStream(this.agentConfPath));
        } catch (IOException e) {
            log.error("{} 配置文件读取失败 Agent启动失败",this.agentConfPath);
            System.exit(0);
        }
        init();
        initWork();
        initStep();
        initOracle();
        initTomcat();
        initZk();
    }

    private void init(){

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_PORT))){
            try {
                this.agentPort = Integer.parseInt(agentConf.getProperty(CONF_AGENT_PORT));
            } catch (NumberFormatException e) {
                System.err.println("Agent启动失败,端口配置无效:" + agentConf.getProperty(CONF_AGENT_PORT));
                System.exit(0);
            }
        }

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_FALCON_PUSH_URL))){
            log.error("Agent启动失败,未定义falcon push地址配置:{}", CONF_AGENT_FALCON_PUSH_URL);
            System.exit(0);
        }
        this.agentPushUrl = agentConf.getProperty(CONF_AGENT_FALCON_PUSH_URL);

    }

    private void initTomcat(){
        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_TOMCAT_JMX_SERVER_NAME))){
            log.error("Agent启动失败,未定义 tomcat 的 jmx 服务名配置:{}", CONF_AGENT_TOMCAT_JMX_SERVER_NAME);
            System.exit(0);
        }
        this.tomcatJmxServerName = agentConf.getProperty(CONF_AGENT_TOMCAT_JMX_SERVER_NAME);
    }

    private void initZk(){
        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ZK_JMX_SERVER_NAME))){
            log.error("Agent启动失败,未定义 zk 的 jmx 服务名配置:{}", CONF_AGENT_ZK_JMX_SERVER_NAME);
            System.exit(0);
        }
        this.zkJmxServerName = agentConf.getProperty(CONF_AGENT_ZK_JMX_SERVER_NAME);
    }

    private void initOracle(){
        Properties pps = new Properties();
        try {
            pps.load(new FileInputStream(System.getProperty("agent.oracle.conf.path")));
        } catch (IOException e) {
            System.err.println("Oracle监控配置文件未指定,请指定系统属性:agent.oracle.conf.path");
            System.exit(0);
        }
        Enumeration en = pps.propertyNames(); //得到配置文件的名字
        while(en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = pps.getProperty(strKey);
            oracleGenericQueries.put(strKey,strValue);
        }

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_DRIVER))){
            log.error("Agent启动失败,未定义 oracle 的 jdbc 配置:{}", CONF_AGENT_ORACLE_JDBC_DRIVER);
            System.exit(0);
        }
        this.oracleJDBCDriver = agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_DRIVER);

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_URL))){
            log.error("Agent启动失败,未定义 oracle 的 jdbc 配置:{}", CONF_AGENT_ORACLE_JDBC_URL);
            System.exit(0);
        }
        this.oracleJDBCUrl = agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_URL);

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_USERNAME))){
            log.error("Agent启动失败,未定义 oracle 的 jdbc 配置:{}", CONF_AGENT_ORACLE_JDBC_USERNAME);
            System.exit(0);
        }
        this.oracleJDBCUsername = agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_USERNAME);

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_PSWD))){
            log.error("Agent启动失败,未定义 oracle 的 jdbc 配置:{}", CONF_AGENT_ORACLE_JDBC_PSWD);
            System.exit(0);
        }
        this.oracleJDBCPassword = agentConf.getProperty(CONF_AGENT_ORACLE_JDBC_PSWD);
    }

    private void initStep(){
        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ZK_METRICS_STEP))){
            try {
                this.zkStep = Integer.parseInt(agentConf.getProperty(CONF_AGENT_ZK_METRICS_STEP));
                if(this.zkStep >= 24 * 60 * 60){
                    log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, CONF_AGENT_ZK_METRICS_STEP);
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, CONF_AGENT_ZK_METRICS_STEP);
                System.exit(0);
            }
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_TOMCAT_METRICS_STEP))){
            try {
                this.tomcatStep = Integer.parseInt(agentConf.getProperty(CONF_AGENT_TOMCAT_METRICS_STEP));
                if(this.tomcatStep >= 24 * 60 * 60){
                    log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, CONF_AGENT_TOMCAT_METRICS_STEP);
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, CONF_AGENT_TOMCAT_METRICS_STEP);
                System.exit(0);
            }
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_METRICS_STEP))){
            try {
                this.oracleStep = Integer.parseInt(agentConf.getProperty(CONF_AGENT_ORACLE_METRICS_STEP));
                if(this.oracleStep >= 24 * 60 * 60){
                    log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, CONF_AGENT_ORACLE_METRICS_STEP);
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, CONF_AGENT_ORACLE_METRICS_STEP);
                System.exit(0);
            }
        }
    }

    private void initWork(){
        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ENDPOINT))){
            this.agentEndpoint = agentConf.getProperty(CONF_AGENT_ENDPOINT);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ZK_WORK))){
            this.agentZkWork = "true".equals(agentConf.getProperty(CONF_AGENT_ZK_WORK));
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_TOMCAT_WORK))){
            this.agentTomcatWork = "true".equals(agentConf.getProperty(CONF_AGENT_TOMCAT_WORK));
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_WORK))){
            this.agentOracleWork = "true".equals(agentConf.getProperty(CONF_AGENT_ORACLE_WORK));
        }
    }

    public int getOracleStep() {
        return oracleStep;
    }

    public Map<String, String> getOracleGenericQueries() {
        return oracleGenericQueries;
    }

    public int getTomcatStep() {
        return tomcatStep;
    }

    public boolean isAgentTomcatWork() {
        return agentTomcatWork;
    }

    public boolean isAgentZkWork() {
        return agentZkWork;
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

    public String getZkJmxServerName(){
        return this.zkJmxServerName;
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

    public int getZkStep(){
        return this.zkStep;
    }

    public String getTomcatJmxServerName() {
        return tomcatJmxServerName;
    }

    public boolean isAgentOracleWork() {
        return agentOracleWork;
    }

    public String getOracleJDBCDriver() {
        return oracleJDBCDriver;
    }

    public String getOracleJDBCUrl() {
        return oracleJDBCUrl;
    }

    public String getOracleJDBCUsername() {
        return oracleJDBCUsername;
    }

    public String getOracleJDBCPassword() {
        return oracleJDBCPassword;
    }
}
