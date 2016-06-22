/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.oracle;

import com.yiji.falcon.agent.config.AgentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class OracleConnection {
    private static final Logger log = LoggerFactory.getLogger(OracleConnection.class);

    private static Connection connection = null;

    private OracleConnection(){}

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if(connection == null || connection.isClosed()){
            //反射Oracle数据库驱动程序类
            Class.forName(AgentConfiguration.INSTANCE.getOracleJDBCDriver());
            //获取数据库连接
            connection = DriverManager.getConnection(AgentConfiguration.INSTANCE.getOracleJDBCUrl(),
                    AgentConfiguration.INSTANCE.getOracleJDBCUsername(),
                    AgentConfiguration.INSTANCE.getOracleJDBCPassword());
        }
        return connection;
    }

    /**
     * 关闭连接
     */
    public static void close(){
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn("",e);
            }
        }
    }

}
