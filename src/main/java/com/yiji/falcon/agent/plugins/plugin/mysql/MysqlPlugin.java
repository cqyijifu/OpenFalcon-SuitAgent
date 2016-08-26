/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.mysql;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-19 14:34 创建
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.plugins.JDBCPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.jdbc.JDBCUserInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * @author guqiu@yiji.com
 */
public class MysqlPlugin implements JDBCPlugin {

    private final List<JDBCUserInfo> userInfoList = new ArrayList<>();
    private int step;
    private PluginActivateType pluginActivateType;
    private String jdbcConfig = null;


    /**
     * 获取JDBC连接集合
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Override
    public Collection<Connection> getConnections() throws SQLException, ClassNotFoundException {
        Set<Connection> connections = new HashSet<>();
        String driver_jdbc = "com.mysql.jdbc.Driver";
        Class.forName(driver_jdbc);
        for (JDBCUserInfo userInfo : userInfoList) {
            connections.add(DriverManager.getConnection(userInfo.getUrl(), userInfo.getUsername(), userInfo.getPassword()));
        }
        return connections;
    }

    /**
     * 数据库监控语句的配置文件
     * 默认值 插件的简单类名第一个字母小写 加 MetricsConf.properties
     *
     * @return 若不需要语句配置文件, 则设置其返回null
     */
    @Override
    public String metricsConfName() {
        return null;
    }

    /**
     * 配置的数据库连接地址
     *
     * @return 返回与配置文件中配置的地址一样即可, 用于启动判断
     */
    @Override
    public String jdbcConfig() {
        return this.jdbcConfig;
    }

    /**
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 如该服务运行的端口号等
     * 若不需要指定则可返回null
     *
     * @return
     */
    @Override
    public String agentSignName() {
        return null;
    }

    /**
     * 插件监控的服务正常运行时的內建监控报告
     * 若有些特殊的监控值无法用配置文件进行配置监控,可利用此方法进行硬编码形式进行获取
     * 注:此方法只有在监控对象可用时,才会调用,并加入到监控值报告中,一并上传
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Override
    public Collection<FalconReportObject> inbuiltReportObjectsForValid() throws SQLException, ClassNotFoundException {
        Metrics metrics = new Metrics(this);
        return metrics.getReports();
    }

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
        jdbcConfig = properties.get("Mysql.jdbc.auth");
        if(!StringUtils.isEmpty(jdbcConfig)){
            String[] auths = jdbcConfig.split("\\^");
            for (String auth : auths) {
                if (!StringUtils.isEmpty(auth)){
                    String url = auth.substring(auth.indexOf("url=") + 4,auth.indexOf("user=") - 1);
                    String user = auth.substring(auth.indexOf("user=") + 5,auth.indexOf("pswd=") - 1);
                    String pswd = auth.substring(auth.indexOf("pswd=") + 5);
                    JDBCUserInfo userInfo = new JDBCUserInfo(url,user,pswd);
                    userInfoList.add(userInfo);
                }
            }
        }
        step = Integer.parseInt(properties.get("step"));
        pluginActivateType = PluginActivateType.valueOf(properties.get("pluginActivateType"));
    }

    /**
     * 授权登陆配置的key前缀(配置在authorization.properties文件中)
     * 将会通过init方法的map属性中,将符合该插件的授权配置传入,以供插件进行初始化操作
     * <p>
     * 如 authorizationKeyPrefix = authorization.prefix , 并且在配置文件中配置了如下信息:
     * authorization.prefix.xxx1 = xxx1
     * authorization.prefix.xxx2 = xxx2
     * 则init中的map中将会传入该KV:
     * authorization.prefix.xxx1 : xxx1
     * authorization.prefix.xxx2 : xxx2
     *
     * @return 若不覆盖此方法, 默认返回空, 既该插件无需授权配置
     */
    @Override
    public String authorizationKeyPrefix() {
        return "Mysql";
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "mysql";
    }

    /**
     * 监控值的获取和上报周期(秒)
     *
     * @return
     */
    @Override
    public int step() {
        return this.step;
    }

    /**
     * 插件运行方式
     *
     * @return
     */
    @Override
    public PluginActivateType activateType() {
        return this.pluginActivateType;
    }
}
