/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.util.PropertiesUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 利用JDBC获取metrics监控值抽象类
 * @author guqiu@yiji.com
 */
public class JDBCMetricsValue {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private JDBCPlugin jdbcPlugin;

    public JDBCMetricsValue(JDBCPlugin jdbcPlugin) {
        this.jdbcPlugin = jdbcPlugin;
    }

    /**
     * 获取所有的监控值报告
     * @return
     * @throws IOException
     */
    public Collection<FalconReportObject> getReportObjects() throws IOException {
        Set<FalconReportObject> result = new HashSet<>();
        Map<String,String> allMetrics = getAllMetricsQuery();
        try {
            jdbcPlugin.getConnection();
        } catch (Exception e) {
            log.warn("连接JDBC异常,创建不可用报告",e);
            result.add(MetricsCommon.generatorVariabilityReport(false,jdbcPlugin.agentSignName(),jdbcPlugin.step(),jdbcPlugin,jdbcPlugin.serverName()));
            return result;
        }

        for (Map.Entry<String, String> entry : allMetrics.entrySet()) {
            try {
                String metricsValue = getMetricsValue(entry.getValue());
                if (!StringUtils.isEmpty(metricsValue)){
                    if(!NumberUtils.isNumber(metricsValue)){
                        log.error("JDBC {} 的监控指标:{} 的值:{} ,不能转换为数字,将跳过此监控指标",jdbcPlugin.serverName(),entry.getKey(),metricsValue);
                    }else{
                        FalconReportObject reportObject = new FalconReportObject();
                        reportObject.setMetric(MetricsCommon.getMetricsName(entry.getKey()));
                        reportObject.setCounterType(CounterType.GAUGE);
                        reportObject.setValue(metricsValue);
                        reportObject.setTimestamp(System.currentTimeMillis() / 1000);
                        reportObject.appendTags(MetricsCommon.getTags(jdbcPlugin.agentSignName(),jdbcPlugin,jdbcPlugin.serverName(), MetricsType.SQLCONF));
                        MetricsCommon.setReportCommonValue(reportObject,jdbcPlugin.step());

                        result.add(reportObject);
                    }
                }else{
                    log.warn("JDBC {} 的监控指标:{} 未获取到值,将跳过此监控指标",jdbcPlugin.serverName(),entry.getKey());
                }
            } catch (Exception e) {
                log.error("SQL 查询异常,跳过监控属性 {}",entry.getKey(),e);
            }
        }

        try {
            //添加內建报告
            Collection<FalconReportObject> inbuilt = jdbcPlugin.inbuiltReportObjectsForValid();
            if(inbuilt != null && !inbuilt.isEmpty()){
                result.addAll(inbuilt);
            }
        } catch (Exception e) {
            log.error("插件內建报告获取异常",e);
        }
        result.add(MetricsCommon.generatorVariabilityReport(true,jdbcPlugin.agentSignName(),jdbcPlugin.step(),jdbcPlugin,jdbcPlugin.serverName()));

        return result;
    }

    /**
     * 获取配置文件所有的监控配置
     * @return
     */
    private Map<String, String> getAllMetricsQuery() {
        return PropertiesUtil.getAllPropertiesByFileName(AgentConfiguration.INSTANCE.getPluginConfPath() + File.separator + jdbcPlugin.metricsConfName());
    }

    /**
     * 获取指定metrics的值
     * @param sql 获取值的sql查询语句
     * @return
     */
    private String getMetricsValue(String sql) throws SQLException, ClassNotFoundException {
        String result = "";
        if(!StringUtils.isEmpty(sql)){
            //创建该连接下的PreparedStatement对象
            PreparedStatement pstmt = jdbcPlugin.getConnection().prepareStatement(sql);

            //执行查询语句，将数据保存到ResultSet对象中
            ResultSet rs = pstmt.executeQuery();

            //将指针移到下一行，判断rs中是否有数据
            if(rs.next()){
                result = rs.getString(1);
            }
            rs.close();
            pstmt.close();
        }
        return result;
    }

}
