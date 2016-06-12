package com.yiji.falcon.agent.plugins.oracle;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/16.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.plugins.JDBCMetricsValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Oracle数据库的监控
 * Created by QianLong on 16/5/16.
 */
public class OracleMetricsValue extends JDBCMetricsValue {

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     *
     * @return
     */
    @Override
    protected Collection<FalconReportObject> getInbuiltReportObjectsForValid() throws SQLException, ClassNotFoundException {
        List<FalconReportObject> result = new ArrayList<>();
        String sql = "SELECT\n" +
                "  Upper(F.TABLESPACE_NAME) \"TSNAME\",\n" +
                "  To_char(Round((D.TOT_GROOTTE_MB - F.TOTAL_BYTES) / D.TOT_GROOTTE_MB * 100, 2), '990.99') \"PERCENT\"\n" +
                "FROM (SELECT\n" +
                "        TABLESPACE_NAME,\n" +
                "        Round(Sum(BYTES) / (1024 * 1024), 2) TOTAL_BYTES,\n" +
                "        Round(Max(BYTES) / (1024 * 1024), 2) MAX_BYTES\n" +
                "      FROM SYS.DBA_FREE_SPACE\n" +
                "      GROUP BY TABLESPACE_NAME) F,\n" +
                "  (SELECT\n" +
                "     DD.TABLESPACE_NAME,\n" +
                "     Round(Sum(DD.BYTES) / (1024 * 1024), 2) TOT_GROOTTE_MB\n" +
                "   FROM SYS.DBA_DATA_FILES DD\n" +
                "   GROUP BY DD.TABLESPACE_NAME) D\n" +
                "WHERE D.TABLESPACE_NAME = F.TABLESPACE_NAME";
        //创建该连接下的PreparedStatement对象
        PreparedStatement pstmt = getConnection().prepareStatement(sql);

        //执行查询语句，将数据保存到ResultSet对象中
        ResultSet rs = pstmt.executeQuery();

        //将指针移到下一行，判断rs中是否有数据
        while (rs.next()){
            String tsName = rs.getString(1);
            String percent = rs.getString(2);

            FalconReportObject falconReportObject = new FalconReportObject();
            setReportCommonValue(falconReportObject);
            falconReportObject.setCounterType(CounterType.GAUGE);
            falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
            falconReportObject.setMetric(getMetricsName("TSUsedPercent-" + tsName.trim(),getName()));
            falconReportObject.setValue(percent.trim());
            falconReportObject.setTags("service.type=database,service=" + getType());
            result.add(falconReportObject);
        }
        rs.close();
        pstmt.close();

        return result;
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
        return OracleConnection.getConnection();
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
