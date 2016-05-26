package com.yiji.falcon.agent.plugins;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/17.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 利用JDBC获取metrics监控值抽象类
 * Created by QianLong on 16/5/17.
 */
public abstract class JDBCMetricsValue extends MetricsCommon{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     * @return
     */
    protected abstract Collection<FalconReportObject> getInbuiltReportObjectsForValid() throws SQLException, ClassNotFoundException;

    /**
     * 获取所有的监控值报告
     * @return
     * @throws IOException
     */
    public Collection<FalconReportObject> getReportObjects() throws IOException {
        Set<FalconReportObject> result = new HashSet<>();
        Map<String,String> allMetrics = getAllMetricsQuery();
        try {
            for (Map.Entry<String, String> entry : allMetrics.entrySet()) {
                String metricsValue = getMetricsValue(entry.getValue());
                if (!StringUtils.isEmpty(metricsValue)){
                    if(!NumberUtils.isNumber(metricsValue)){
                        log.error("JDBC {} 的监控指标:{} 的值:{} ,不能转换为数字,将跳过此监控指标",getType(),entry.getKey(),metricsValue);
                    }else{
                        FalconReportObject reportObject = new FalconReportObject();
                        reportObject.setMetric(getMetricsName(entry.getKey(),getName()));
                        reportObject.setCounterType(CounterType.GAUGE);
                        reportObject.setValue(metricsValue);
                        reportObject.setTimestamp(System.currentTimeMillis() / 1000);
                        setReportCommonValue(reportObject,getName());

                        result.add(reportObject);
                    }
                }else{
                    log.warn("JDBC {} 的监控指标:{} 未获取到值,将跳过此监控指标",getType(),entry.getKey());
                }
            }
            //添加內建报告
            Collection<FalconReportObject> inbuilt = getInbuiltReportObjectsForValid();
            if(inbuilt != null && !inbuilt.isEmpty()){
                result.addAll(inbuilt);
            }
            result.add(generatorVariabilityReport(true,getName()));
        } catch (SQLException | ClassNotFoundException e) {
            log.warn("连接JDBC异常,创建不可用报告",e);
            result.add(generatorVariabilityReport(false,getName()));
        }
        return result;
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
            PreparedStatement pstmt = getConnection().prepareStatement(sql);

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

    /**
     * 所有的metrics的查询语句
     * metrics指标名 : 对应的查询语句
     * @return
     */
    public abstract Map<String,String> getAllMetricsQuery();



    /**
     * 设置报告对象公共的属性
     * endpoint
     * step
     * @param falconReportObject
     */
    @Override
    public void setReportCommonValue(FalconReportObject falconReportObject,String name){
        if(falconReportObject != null){
            falconReportObject.setEndpoint(AgentConfiguration.INSTANCE.getAgentEndpoint());
            falconReportObject.setStep(getStep());
        }
    }

    /**
     * 获取JDBC连接
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public abstract Connection getConnection() throws SQLException,ClassNotFoundException;

    /**
     * 获取step
     * @return
     */
    public abstract int getStep();

    /**
     * 报告对象的连接标识名
     * @return
     */
    public abstract String getName();
}
