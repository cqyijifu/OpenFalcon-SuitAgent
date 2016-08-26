/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-23 15:46 创建
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * JDBC 插件接口
 * @author guqiu@yiji.com
 */
public interface JDBCPlugin extends Plugin{


    /**
     * 获取JDBC连接集合
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    Collection<Connection> getConnections() throws SQLException, ClassNotFoundException;

    /**
     * 数据库监控语句的配置文件
     * 默认值 插件的简单类名第一个字母小写 加 MetricsConf.properties
     * @return
     * 若不需要语句配置文件,则设置其返回null
     */
    default String metricsConfName(){
        String className = this.getClass().getSimpleName();
        return className.substring(0,1).toLowerCase() + className.substring(1) + "MetricsConf.properties";
    }

    /**
     * 配置的数据库连接地址
     *
     * @return
     * 返回与配置文件中配置的地址一样即可,用于启动判断
     */
    String jdbcConfig();

    /**
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 如该服务运行的端口号等
     * 若不需要指定则可返回null
     * @return
     */
    String agentSignName();

    /**
     * 插件监控的服务正常运行时的內建监控报告
     * 若有些特殊的监控值无法用配置文件进行配置监控,可利用此方法进行硬编码形式进行获取
     * 注:此方法只有在监控对象可用时,才会调用,并加入到监控值报告中,一并上传
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    Collection<FalconReportObject> inbuiltReportObjectsForValid() throws SQLException, ClassNotFoundException;

    /**
     * 关闭数据库连接的工具方法
     * @param connections
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    default void helpCloseConnections(Collection<Connection> connections) throws SQLException, ClassNotFoundException {
        if(connections != null){
            connections.stream().filter(connection -> null != connection).forEach(connection -> {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            });
        }
    }

}
