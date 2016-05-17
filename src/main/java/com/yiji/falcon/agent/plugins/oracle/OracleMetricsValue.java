package com.yiji.falcon.agent.plugins.oracle;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/16.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.plugins.JDBCMetricsValue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Oracle数据库的监控
 * Created by QianLong on 16/5/16.
 */
public class OracleMetricsValue extends JDBCMetricsValue {
    private Connection connection = null;

    public OracleMetricsValue() throws SQLException, ClassNotFoundException {
        connection = OracleConnection.getConnection();
    }

    /**
     * 所有的metrics的查询语句
     * metrics指标名 : 对应的查询语句
     *
     * @return
     */
    @Override
    public Map<String, String> getAllMetricsQuery() {
        return AgentConfiguration.INSTANCE.getOracleGenericQueries();
    }

    /**
     * 获取JDBC连接
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Override
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        return connection;
    }

    /**
     * 获取step
     *
     * @return
     */
    @Override
    public int getStep() {
        return AgentConfiguration.INSTANCE.getOracleStep();
    }

    /**
     * 监控类型
     *
     * @return
     */
    @Override
    public String getType() {
        return "Oracle";
    }

    /**
     * 报告对象的连接标识名
     *
     * @return
     */
    @Override
    public String getName() {
        return null;
    }
}
