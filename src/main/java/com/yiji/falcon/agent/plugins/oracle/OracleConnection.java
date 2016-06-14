package com.yiji.falcon.agent.plugins.oracle;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/16.
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by QianLong on 16/5/16.
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
