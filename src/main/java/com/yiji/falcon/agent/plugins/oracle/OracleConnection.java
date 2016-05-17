package com.yiji.falcon.agent.plugins.oracle;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/16.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
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

    private static String JDBC_DRIVER = AgentConfiguration.INSTANCE.getOracleJDBCDriver();
    private static String JDBC_URL = AgentConfiguration.INSTANCE.getOracleJDBCUrl();
    private static String JDBC_USER = AgentConfiguration.INSTANCE.getOracleJDBCUsername();
    private static String JDBC_PASSWORD = AgentConfiguration.INSTANCE.getOracleJDBCPassword();

    private static Connection connection = null;

    private OracleConnection(){}

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if(connection == null){
            //反射Oracle数据库驱动程序类
            Class.forName(JDBC_DRIVER);
            //获取数据库连接
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
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
