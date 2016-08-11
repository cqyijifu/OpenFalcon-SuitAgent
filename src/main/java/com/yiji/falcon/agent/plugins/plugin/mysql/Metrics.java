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
public class Metrics {

    private JDBCPlugin plugin;

    public Metrics(JDBCPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取监控值
     * @return
     */
    public Collection<FalconReportObject> getReports() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();
        reportObjectSet.addAll(getGlobalStatus());
        reportObjectSet.addAll(getGlobalVariables());
//        reportObjectSet.addAll(getInnodbStatus());
//        reportObjectSet.addAll(getSalveStatus());
        return reportObjectSet;
    }

//    private Collection<? extends FalconReportObject> getSalveStatus() throws SQLException{
//        Set<FalconReportObject> reportObjectSet = new HashSet<>();
//        return reportObjectSet;
//    }

    private Collection<? extends FalconReportObject> getGlobalVariables() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();
        String sql = "SHOW /*!50001 GLOBAL */ VARIABLES";
        Collection<Connection> connections = plugin.getConnections();
        for (Connection connection : connections) {
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
        }
        plugin.helpCloseConnections(connections);
        return reportObjectSet;
    }

//    private Collection<? extends FalconReportObject> getInnodbStatus() throws SQLException{
//        Set<FalconReportObject> reportObjectSet = new HashSet<>();
//        return reportObjectSet;
//    }

    private Collection<? extends FalconReportObject> getGlobalStatus() throws SQLException, ClassNotFoundException {
        Set<FalconReportObject> reportObjectSet = new HashSet<>();
        String sql = "SHOW /*!50001 GLOBAL */ STATUS";
        Collection<Connection> connections = plugin.getConnections();
        for (Connection connection : connections) {
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
        }
        plugin.helpCloseConnections(connections);
        return reportObjectSet;
    }

}
