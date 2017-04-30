/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.metrics;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.JDBCPlugin;
import com.yiji.falcon.agent.util.PropertiesUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.jdbc.JDBCConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 利用JDBC获取metrics监控值抽象类
 *
 * @author guqiu@yiji.com
 */
@Slf4j
public class JDBCMetricsValue extends MetricsCommon {
    private JDBCPlugin jdbcPlugin;

//    private Collection<Connection> connections;

    public JDBCMetricsValue(JDBCPlugin jdbcPlugin) {
        this.jdbcPlugin = jdbcPlugin;
    }

    /**
     * 获取可用性报告
     *
     * @param valid
     * @param connectionInfo
     * @return
     */
    private FalconReportObject getVariabilityReport(boolean valid, JDBCConnectionInfo connectionInfo) {
        FalconReportObject falconReportObject = MetricsCommon.generatorVariabilityReport(valid, jdbcPlugin.agentSignName(), jdbcPlugin.step(), jdbcPlugin, jdbcPlugin.serverName());
        //tag上添加数据库连接URL，以便区分多个数据库连接实例
        addURLTag(falconReportObject, connectionInfo);
        return falconReportObject;
    }

    /**
     * tag上添加数据库连接URL，以便区分多个数据库连接实例
     *
     * @param falconReportObject
     * @param connectionInfo
     */
    private void addURLTag(FalconReportObject falconReportObject, JDBCConnectionInfo connectionInfo) {
        falconReportObject.appendTags("url=" + connectionInfo.getUrl()
                .replace("jdbc:mysql://","")
                .replace("jdbc:oracle:thin:@",""));
    }

    /**
     * 获取所有的监控值报告
     *
     * @return
     * @throws IOException
     */
    @Override
    public Collection<FalconReportObject> getReportObjects() {
        Set<FalconReportObject> result = new HashSet<>();
        Map<String, String> allMetrics = getAllMetricsQuery();
        String driverName = jdbcPlugin.getJDBCDriveName();
        if (StringUtils.isEmpty(driverName)) {
            log.error("从插件{}获取数据库驱动名称为空，无法构建数据库连接", jdbcPlugin.pluginName());
            return result;
        }
        try {
            Class.forName(driverName);
        } catch (Exception e) {
            log.error("数据库驱动加载异常", e);
            return result;
        }
        Collection<JDBCConnectionInfo> connectionInfos = jdbcPlugin.getConnectionInfos();
        if (connectionInfos != null && !connectionInfos.isEmpty()) {
            for (JDBCConnectionInfo connectionInfo : connectionInfos) {
                try (
                        Connection connection = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUsername(), connectionInfo.getPassword())
                ) {
                    for (Map.Entry<String, String> entry : allMetrics.entrySet()) {
                        try {
                            String metricsValue = getMetricsValue(entry.getValue(), connection);
                            if (!StringUtils.isEmpty(metricsValue)) {
                                if (!NumberUtils.isNumber(metricsValue)) {
                                    log.error("JDBC {} 的监控指标:{} 的值:{} ,不能转换为数字,将跳过此监控指标", jdbcPlugin.serverName(), entry.getKey(), metricsValue);
                                } else {
                                    FalconReportObject reportObject = new FalconReportObject();
                                    reportObject.setMetric(MetricsCommon.getMetricsName(entry.getKey()));
                                    reportObject.setCounterType(CounterType.GAUGE);
                                    reportObject.setValue(metricsValue);
                                    reportObject.setTimestamp(System.currentTimeMillis() / 1000);
                                    reportObject.appendTags(MetricsCommon.getTags(jdbcPlugin.agentSignName(), jdbcPlugin, jdbcPlugin.serverName(), MetricsType.SQL_CONF));
                                    addURLTag(reportObject, connectionInfo);
                                    MetricsCommon.setReportCommonValue(reportObject, jdbcPlugin.step());

                                    result.add(reportObject);
                                }
                            } else {
                                log.warn("JDBC {} 的监控指标:{} 未获取到值,将跳过此监控指标", jdbcPlugin.serverName(), entry.getKey());
                            }
                        } catch (Exception e) {
                            log.error("SQL 查询异常,跳过监控属性 {}", entry.getKey(), e);
                        }
                    }

                    try {
                        //添加內建报告
                        Collection<FalconReportObject> inbuilt = jdbcPlugin.inbuiltReportObjectsForValid(connection);
                        if (inbuilt != null) {
                            for (FalconReportObject falconReportObject : inbuilt) {
                                addURLTag(falconReportObject, connectionInfo);
                                result.add(falconReportObject);
                            }
                        }
                    } catch (Exception e) {
                        log.error("插件內建报告获取异常", e);
                    }
                    result.add(getVariabilityReport(true, connectionInfo));

                } catch (Exception e) {
                    log.warn("连接JDBC异常,创建不可用报告", e);
                    result.add(getVariabilityReport(false, connectionInfo));
                }
            }
        }

        return result;
    }

    /**
     * 获取配置文件所有的监控配置
     *
     * @return
     */
    private Map<String, String> getAllMetricsQuery() {
        if (!StringUtils.isEmpty(jdbcPlugin.metricsConfName())) {
            return PropertiesUtil.getAllPropertiesByFileName(AgentConfiguration.INSTANCE.getPluginConfPath() + File.separator + jdbcPlugin.metricsConfName());
        } else {
            return new HashMap<>();
        }
    }

    /**
     * 获取指定metrics的值
     *
     * @param sql 获取值的sql查询语句
     * @return
     */
    private String getMetricsValue(String sql, Connection connection) throws SQLException, ClassNotFoundException {
        String result = "";
        if (!StringUtils.isEmpty(sql)) {
            //创建该连接下的PreparedStatement对象
            PreparedStatement pstmt = connection.prepareStatement(sql);
            //执行查询语句，将数据保存到ResultSet对象中
            ResultSet rs = pstmt.executeQuery();
            //将指针移到下一行，判断rs中是否有数据
            if (rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
            pstmt.close();
        }
        return result;
    }

}
