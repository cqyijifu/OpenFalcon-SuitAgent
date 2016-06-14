package com.yiji.falcon.agent.config;/**
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
     * 服务配置文件路径
     */
    private String agentConfPath = null;
    private String jmxCommonMetricsConfPath = null;
    private String zkMetricsConfPath = null;
    private String tomcatMetricsConfPath = null;
    private String elasticSearchMetricsConfPath = null;

    /**
     * agent监控指标的主体说明
     */
    private String agentEndpoint = "";
    /**
     * 数据采集周期
     */
    private int zkStep = 60;
    private int tomcatStep = 60;
    private int oracleStep = 60;
    private int elasticSearchStep = 60;
    private int logstashStep = 60;

    /**
     * JMX 连接服务名
     */
    private String zkJmxServerName = null;
    private String tomcatJmxServerName = null;
    private String elasticSearchJmxServerName = null;
    private String logstashJmxServerName = null;

    /**
     * agent启动端口
     */
    private int agentPort = 4518;

    private final String falseStr = "false";
    //服务开启项
    private String agentZkWork = falseStr;
    private String agentTomcatWork = falseStr;
    private String agentOracleWork = falseStr;
    private String agentElasticSearchWork = falseStr;
    private String agentLogstashWork = falseStr;

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
    private static final String CONF_AGENT_ELASTICSEARCH_METRICS_STEP = "agent.elasticSearch.metrics.step";
    private static final String CONF_AGENT_LOGSTASH_METRICS_STEP = "agent.logstash.metrics.step";

    private static final String CONF_AGENT_FALCON_PUSH_URL = "agent.falcon.push.url";
    private static final String CONF_AGENT_PORT = "agent.port";

    private static final String CONF_AGENT_ZK_JMX_SERVER_NAME = "agent.zk.jmx.serverName";
    private static final String CONF_AGENT_TOMCAT_JMX_SERVER_NAME = "agent.tomcat.jmx.serverName";
    private static final String CONF_AGENT_ELASTICSEARCH_JMX_SERVER_NAME = "agent.elasticSearch.jmx.serverName";
    private static final String CONF_AGENT_LOGSTASH_JMX_SERVER_NAME = "agent.logstash.jmx.serverName";

    private static final String CONF_AGENT_ZK_WORK = "agent.zk.work";
    private static final String CONF_AGENT_ORACLE_WORK = "agent.oracle.work";
    private static final String CONF_AGENT_TOMCAT_WORK = "agent.tomcat.work";
    private static final String CONF_AGENT_ELASTICSEARCH_WORK = "agent.elasticSearch.work";
    private static final String CONF_AGENT_LOGSTASH_WORK = "agent.logstash.work";

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
            log.error("agent 配置文件位置读取失败,请确定系统配置项:" + "agent.conf.path");
            System.exit(0);
        }else{
            this.agentConfPath = System.getProperty("agent.conf.path");
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
        try {
            agentConf.load(new FileInputStream(this.agentConfPath));
        } catch (IOException e) {
            log.error("{} 配置文件读取失败 Agent启动失败",this.agentConfPath);
            System.exit(0);
        }
        init();
        initJMXCommon();
        initWork();
        initStep();
        initOracle();
        initTomcat();
        initZk();
        initElasticSearch();
        initLogstash();
    }

    private void init(){

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_PORT))){
            try {
                this.agentPort = Integer.parseInt(agentConf.getProperty(CONF_AGENT_PORT));
            } catch (NumberFormatException e) {
                log.error("Agent启动失败,端口配置无效:" + agentConf.getProperty(CONF_AGENT_PORT));
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
        String tomcatConfPath = System.getProperty("agent.jmx.metrics.tomcat.path");
        if(StringUtils.isEmpty(tomcatConfPath)){
            log.error("tomcat的jmx配置系统属性文件未定义:agent.jmx.metrics.tomcat.path");
            System.exit(0);
        }
        this.tomcatMetricsConfPath = tomcatConfPath;

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_TOMCAT_JMX_SERVER_NAME))){
            log.error("Agent启动失败,未定义 tomcat 的 jmx 服务名配置:{}", CONF_AGENT_TOMCAT_JMX_SERVER_NAME);
            System.exit(0);
        }
        this.tomcatJmxServerName = agentConf.getProperty(CONF_AGENT_TOMCAT_JMX_SERVER_NAME);
    }

    private void initZk(){
        String zkConfPath = System.getProperty("agent.jmx.metrics.zk.path");
        if(StringUtils.isEmpty(zkConfPath)){
            log.error("zookeeper的jmx配置系统属性文件未定义:agent.jmx.metrics.zk.path");
            System.exit(0);
        }
        this.zkMetricsConfPath = zkConfPath;

        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ZK_JMX_SERVER_NAME))){
            log.error("Agent启动失败,未定义 zk 的 jmx 服务名配置:{}", CONF_AGENT_ZK_JMX_SERVER_NAME);
            System.exit(0);
        }
        this.zkJmxServerName = agentConf.getProperty(CONF_AGENT_ZK_JMX_SERVER_NAME);
    }

    private void initElasticSearch(){
        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ELASTICSEARCH_JMX_SERVER_NAME))){
            log.error("Agent启动失败,未定义 zk 的 jmx 服务名配置:{}", CONF_AGENT_ELASTICSEARCH_JMX_SERVER_NAME);
            System.exit(0);
        }
        this.elasticSearchJmxServerName = agentConf.getProperty(CONF_AGENT_ELASTICSEARCH_JMX_SERVER_NAME);

        String property = System.getProperty("agent.metrics.elasticSearch.path");
        if(StringUtils.isEmpty(property)){
            log.error("tomcat的jmx配置系统属性文件未定义:agent.metrics.elasticSearch.path");
            System.exit(0);
        }
        this.elasticSearchMetricsConfPath = property;
    }

    private void initLogstash(){
        if(StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_LOGSTASH_JMX_SERVER_NAME))){
            log.error("Agent启动失败,未定义 zk 的 jmx 服务名配置:{}", CONF_AGENT_LOGSTASH_JMX_SERVER_NAME);
            System.exit(0);
        }
        this.logstashJmxServerName = agentConf.getProperty(CONF_AGENT_LOGSTASH_JMX_SERVER_NAME);
    }

    private void initJMXCommon(){
        String property = System.getProperty("agent.jmx.metrics.common.path");
        if(StringUtils.isEmpty(property)){
            log.error("jmx 的公共配置系统属性文件未定义:agent.jmx.metrics.common.path");
            System.exit(0);
        }
        this.jmxCommonMetricsConfPath = property;
    }

    private void initOracle(){
        Properties pps = new Properties();
        try {
            pps.load(new FileInputStream(System.getProperty("agent.oracle.conf.path")));
        } catch (IOException e) {
            log.error("Oracle监控配置文件未指定,请指定系统属性:agent.oracle.conf.path",e);
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
        setInitStep(CONF_AGENT_ZK_METRICS_STEP,4);
        setInitStep(CONF_AGENT_TOMCAT_METRICS_STEP,3);
        setInitStep(CONF_AGENT_ORACLE_METRICS_STEP,2);
        setInitStep(CONF_AGENT_ELASTICSEARCH_METRICS_STEP,1);
        setInitStep(CONF_AGENT_LOGSTASH_METRICS_STEP,5);
    }

    /**
     * 设置step配置
     * @param stepConf
     * step配置名
     * @param type
     * 设置的类型
     */
    private void setInitStep(String stepConf,int type){
        if(!StringUtils.isEmpty(agentConf.getProperty(stepConf))){
            try {
                int value = Integer.parseInt(agentConf.getProperty(stepConf));
                if(value >= 24 * 60 * 60){
                    log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, stepConf);
                    System.exit(0);
                }
                switch (type){
                    case 1 :
                        this.elasticSearchStep = value;
                        break;

                    case 2:
                        this.oracleStep = value;
                        break;
                    case 3:
                        this.tomcatStep = value;
                        break;
                    case 4:
                        this.zkStep = value;
                        break;
                    case 5:
                        this.logstashStep = value;
                        break;
                    default:break;
                }
            } catch (NumberFormatException e) {
                log.error("配置文件: {} 的 {} 配置的值非法 Agent启动失败",this.agentConfPath, stepConf);
                System.exit(0);
            }
        }
    }

    private void initWork(){
        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ENDPOINT))){
            this.agentEndpoint = agentConf.getProperty(CONF_AGENT_ENDPOINT);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ZK_WORK))){
            this.agentZkWork = agentConf.getProperty(CONF_AGENT_ZK_WORK);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_TOMCAT_WORK))){
            this.agentTomcatWork = agentConf.getProperty(CONF_AGENT_TOMCAT_WORK);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ORACLE_WORK))){
            this.agentOracleWork = agentConf.getProperty(CONF_AGENT_ORACLE_WORK);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_ELASTICSEARCH_WORK))){
            this.agentElasticSearchWork = agentConf.getProperty(CONF_AGENT_ELASTICSEARCH_WORK);
        }

        if(!StringUtils.isEmpty(agentConf.getProperty(CONF_AGENT_LOGSTASH_WORK))){
            this.agentLogstashWork = agentConf.getProperty(CONF_AGENT_LOGSTASH_WORK);
        }

    }

    public int getElasticSearchStep() {
        return elasticSearchStep;
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

    public String getZkMetricsConfPath() {
        return zkMetricsConfPath;
    }

    public String getTomcatMetricsConfPath() {
        return tomcatMetricsConfPath;
    }

    public String getJmxCommonMetricsConfPath() {
        return jmxCommonMetricsConfPath;
    }

    public String getElasticSearchJmxServerName() {
        return elasticSearchJmxServerName;
    }

    public String getElasticSearchMetricsConfPath() {
        return elasticSearchMetricsConfPath;
    }

    public int getLogstashStep() {
        return logstashStep;
    }

    public String getLogstashJmxServerName() {
        return logstashJmxServerName;
    }

    public String getAgentZkWork() {
        return agentZkWork;
    }

    public String getAgentTomcatWork() {
        return agentTomcatWork;
    }

    public String getAgentOracleWork() {
        return agentOracleWork;
    }

    public String getAgentElasticSearchWork() {
        return agentElasticSearchWork;
    }

    public String getAgentLogstashWork() {
        return agentLogstashWork;
    }
}
