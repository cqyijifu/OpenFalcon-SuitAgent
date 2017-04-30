/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.mysql;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-19 15:39 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.JDBCPlugin;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 监控值收集
 * @author guqiu@yiji.com
 */
class Metrics {

    private JDBCPlugin plugin;
    private Connection connection;

    Metrics(JDBCPlugin plugin,Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    /**
     * 获取监控值
     * @return
     */
    Collection<FalconReportObject> getReports() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();

        reportObjectSet.addAll(getGlobalStatus());
        reportObjectSet.addAll(getGlobalVariables());
//        reportObjectSet.addAll(getInnodbStatus());
        reportObjectSet.addAll(getSalveStatus());

        return reportObjectSet;
    }

    private Collection<? extends FalconReportObject> getSalveStatus() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();
        String sql = "show slave status";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()){
            String value_Slave_IO_Running = rs.getString("Slave_IO_Running");
            String value_Slave_SQL_Running = rs.getString("Slave_SQL_Running");
            String value_Seconds_Behind_Master = rs.getString("Seconds_Behind_Master");
            String value_Connect_Retry = rs.getString("Connect_Retry");

            FalconReportObject falconReportObject = new FalconReportObject();
            MetricsCommon.setReportCommonValue(falconReportObject,plugin.step());
            falconReportObject.setCounterType(CounterType.GAUGE);
            falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
            falconReportObject.appendTags(MetricsCommon.getTags(plugin.agentSignName(),plugin,plugin.serverName(), MetricsType.SQL_IN_BUILD));

            //Slave_IO_Running
            falconReportObject.setMetric("Slave_IO_Running");
            if(value_Slave_IO_Running.equals("No") || value_Slave_IO_Running.equals("Connecting")){
                falconReportObject.setValue("0");
            }else{
                falconReportObject.setValue("1");
            }
            reportObjectSet.add(falconReportObject.clone());

            //Slave_SQL_Running
            falconReportObject.setMetric("Slave_SQL_Running");
            falconReportObject.setValue("yes".equals(value_Slave_SQL_Running.toLowerCase()) ? "1" : "0");
            reportObjectSet.add(falconReportObject.clone());

            //Seconds_Behind_Master
            falconReportObject.setMetric("Seconds_Behind_Master");
            falconReportObject.setValue(value_Seconds_Behind_Master == null ? "0" : value_Seconds_Behind_Master);
            reportObjectSet.add(falconReportObject.clone());

            //Connect_Retry
            falconReportObject.setMetric("Connect_Retry");
            falconReportObject.setValue(value_Connect_Retry);
            reportObjectSet.add(falconReportObject.clone());

        }
        return reportObjectSet;
    }

    private Collection<? extends FalconReportObject> getGlobalVariables() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();
        String sql = "SHOW /*!50001 GLOBAL */ VARIABLES";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()){
            String metric = rs.getString(1);
            String value = rs.getString(2);
            try {
                //收集值为数字的结果
                long v = Long.parseLong(value);
                FalconReportObject falconReportObject = new FalconReportObject();
                MetricsCommon.setReportCommonValue(falconReportObject,plugin.step());
                falconReportObject.setCounterType(CounterType.GAUGE);
                falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
                falconReportObject.setMetric(metric);
                falconReportObject.setValue(v + "");
                falconReportObject.appendTags(MetricsCommon.getTags(plugin.agentSignName(),plugin,plugin.serverName(), MetricsType.SQL_IN_BUILD));
                reportObjectSet.add(falconReportObject);
            } catch (NumberFormatException ignored) {
            }

        }
        rs.close();
        pstmt.close();
        return reportObjectSet;
    }

//    private Collection<? extends FalconReportObject> getInnodbStatus() throws SQLException{
//        Set<FalconReportObject> reportObjectSet = new HashSet<>();
//        return reportObjectSet;
//    }

    private Collection<? extends FalconReportObject> getGlobalStatus() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();
        String sql = "SHOW /*!50001 GLOBAL */ STATUS";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()){
            String metric = rs.getString(1);
            String value = rs.getString(2);

            FalconReportObject falconReportObject = new FalconReportObject();
            MetricsCommon.setReportCommonValue(falconReportObject,plugin.step());
            falconReportObject.setCounterType(CounterType.GAUGE);
            falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
            falconReportObject.setMetric(metric);
            falconReportObject.setValue(value);
            falconReportObject.appendTags(MetricsCommon.getTags(plugin.agentSignName(),plugin,plugin.serverName(), MetricsType.SQL_IN_BUILD));
            reportObjectSet.add(falconReportObject);
        }
        rs.close();
        pstmt.close();
        return reportObjectSet;
    }

}
