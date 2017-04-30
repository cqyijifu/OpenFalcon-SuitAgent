/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.oracle;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-28 11:07 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.plugins.JDBCPlugin;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.jdbc.JDBCConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.yiji.falcon.agent.plugins.metrics.MetricsCommon.getMetricsName;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class OraclePlugin implements JDBCPlugin {
    private final List<JDBCConnectionInfo> connectionInfos = new ArrayList<>();
    private int step;
    private PluginActivateType pluginActivateType;
    private String jdbcConfig = null;

    private final static String tbSql = "SELECT dtp.tablespace_name ts_name,\n" +
            "       NVL(ts.bytes, 0) / 1024 / 1024 size_m,\n" +
            "       NVL(m_bk.m_bt, 0) / 1024 / 1024 size_max_m,\n" +
            "       NVL(ts.bytes - NVL(f.bytes, 0), 0) / 1024 / 1024 used_m,\n" +
            "       ROUND((NVL(ts.bytes - NVL(f.bytes, 0), 0) / NVL(ts.bytes, 0) * 100),\n" +
            "             2) used_percent,\n" +
            "       ROUND((NVL(ts.bytes - NVL(f.bytes, 0), 0) / 1024 / 1024) /\n" +
            "             (nvl(m_bk.m_bt, 0) / 1024 / 1024) * 100,\n" +
            "             2) real_used_percent\n" +
            "  FROM sys.DBA_TABLESPACES dtp,\n" +
            "       (SELECT tablespace_name, SUM(bytes) bytes\n" +
            "          FROM DBA_DATA_FILES\n" +
            "         GROUP BY tablespace_name) ts,\n" +
            "       (SELECT tablespace_name, SUM(bytes) bytes\n" +
            "          FROM DBA_FREE_SPACE\n" +
            "         GROUP BY tablespace_name) f,\n" +
            "       (select sum(max_b) m_bt, tablespace_name\n" +
            "          from (select case\n" +
            "                         when MAXBYTES = 0 then\n" +
            "                          bytes\n" +
            "                         else\n" +
            "                          MAXBYTES\n" +
            "                       end MAX_B,\n" +
            "                       tablespace_name\n" +
            "                  from dba_data_files)\n" +
            "         group by tablespace_name) m_bk\n" +
            " WHERE dtp.tablespace_name = ts.tablespace_name(+)\n" +
            "   AND dtp.tablespace_name = f.tablespace_name(+)\n" +
            "   and dtp.tablespace_name = m_bk.tablespace_name(+)\n" +
            "   AND NOT (dtp.CONTENTS LIKE 'TEMPORARY')\n" +
            "union all\n" +
            "SELECT dtp.tablespace_name ts_name,\n" +
            "       NVL(a.bytes, 0) / 1024 / 1024 size_m,\n" +
            "        NVL(m_bk.m_bt, 0) / 1024 / 1024 size_max_m,\n" +
            "       NVL(t.bytes, 0) / 1024 / 1024 used_m,\n" +
            "       ROUND(NVL(t.bytes, 0) / NVL(a.bytes, 0) * 100, 2) used_percent,\n" +
            "       ROUND(NVL(t.bytes, 0) /(nvl(m_bk.m_bt, 0)) * 100,2) real_used_percent\n" +
            "  FROM sys.DBA_TABLESPACES dtp,\n" +
            "       (SELECT tablespace_name, SUM(bytes) bytes\n" +
            "          FROM DBA_TEMP_FILES\n" +
            "         GROUP BY tablespace_name) a,\n" +
            "       (SELECT ss.tablespace_name,\n" +
            "               SUM((ss.used_blocks * ts.blocksize)) bytes\n" +
            "          FROM gv$sort_segment ss, sys.ts$ ts\n" +
            "         WHERE ss.tablespace_name = ts.name\n" +
            "         GROUP BY ss.tablespace_name) t,\n" +
            "        (select sum(max_b) m_bt, tablespace_name\n" +
            "          from (select case\n" +
            "                         when MAXBYTES = 0 then\n" +
            "                          bytes\n" +
            "                         else\n" +
            "                          MAXBYTES\n" +
            "                       end MAX_B,\n" +
            "                       tablespace_name\n" +
            "                  from dba_temp_files)\n" +
            "         group by tablespace_name) m_bk\n" +
            " WHERE dtp.tablespace_name = a.tablespace_name(+)\n" +
            "   AND dtp.tablespace_name = t.tablespace_name(+)\n" +
            "   and dtp.tablespace_name = m_bk.tablespace_name(+)\n" +
            "   AND dtp.CONTENTS LIKE 'TEMPORARY'";

    @Override
    public String authorizationKeyPrefix() {
        return "Oracle";
    }


    /**
     * 数据库的JDBC连接驱动名称
     *
     * @return
     */
    @Override
    public String getJDBCDriveName() {
        return "oracle.jdbc.driver.OracleDriver";
    }

    /**
     * 数据库的连接对象集合
     * 系统将根据此对象建立数据库连接
     *
     * @return
     */
    @Override
    public Collection<JDBCConnectionInfo> getConnectionInfos() {
        return connectionInfos;
    }

    /**
     * 配置的数据库连接地址
     *
     * @return 返回与配置文件中配置的地址一样即可, 用于启动判断
     */
    @Override
    public String jdbcConfig() {
        return this.jdbcConfig;
    }

    /**
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 如该服务运行的端口号等
     * 若不需要指定则可返回null
     *
     * @return
     */
    @Override
    public String agentSignName() {
        return null;
    }

    /**
     * 插件监控的服务正常运行时的內建监控报告
     * 若有些特殊的监控值无法用配置文件进行配置监控,可利用此方法进行硬编码形式进行获取
     * 注:此方法只有在监控对象可用时,才会调用,并加入到监控值报告中,一并上传
     * @param connection
     * 数据库连接 不需在方法内关闭连接
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Override
    public Collection<FalconReportObject> inbuiltReportObjectsForValid(Connection connection) throws SQLException, ClassNotFoundException {
        List<FalconReportObject> result = new ArrayList<>();
        //创建该连接下的PreparedStatement对象
        PreparedStatement pstmt = connection.prepareStatement(tbSql);
        //执行查询语句，将数据保存到ResultSet对象中
        ResultSet rs = pstmt.executeQuery();
        //将指针移到下一行，判断rs中是否有数据
        while (rs.next()){
            String tsName = rs.getString("TS_NAME");
            String size = rs.getString("SIZE_M");
            String sizeMax = rs.getString("SIZE_MAX_M");
            String used = rs.getString("USED_M");
            String usedPercent = rs.getString("USED_PERCENT");
            String realUsedPercent = rs.getString("REAL_USED_PERCENT");

            FalconReportObject falconReportObject = new FalconReportObject();
            MetricsCommon.setReportCommonValue(falconReportObject,step());
            falconReportObject.setCounterType(CounterType.GAUGE);
            falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
            falconReportObject.appendTags(MetricsCommon.getTags(agentSignName(),this,serverName(), MetricsType.SQL_IN_BUILD))
                    .appendTags("TSName=" + tsName.trim());

            falconReportObject.setMetric(getMetricsName("ts.size"));
            falconReportObject.setValue(size);
            result.add(falconReportObject.clone());

            falconReportObject.setMetric(getMetricsName("ts.size.max"));
            falconReportObject.setValue(sizeMax);
            result.add(falconReportObject.clone());

            falconReportObject.setMetric(getMetricsName("ts.used"));
            falconReportObject.setValue(used);
            result.add(falconReportObject.clone());

            falconReportObject.setMetric(getMetricsName("ts.used.percent"));
            falconReportObject.setValue(usedPercent);
            result.add(falconReportObject.clone());

            falconReportObject.setMetric(getMetricsName("ts.used.real.percent"));
            falconReportObject.setValue(realUsedPercent);
            result.add(falconReportObject.clone());
        }
        rs.close();
        pstmt.close();

        return result;
    }

    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     * @param properties
     * 包含的配置:
     * 1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     * 2、插件指定的配置文件的全部配置信息(参见 {@link com.yiji.falcon.agent.plugins.Plugin#configFileName()} 接口项)
     * 3、授权配置项(参见 {@link com.yiji.falcon.agent.plugins.Plugin#authorizationKeyPrefix()} 接口项
     */
    @Override
    public void init(Map<String, String> properties) {
        jdbcConfig = properties.get("Oracle.jdbc.auth");
        if(!StringUtils.isEmpty(jdbcConfig)){
            String[] auths = jdbcConfig.split("\\^");
            for (String auth : auths) {
                if (!StringUtils.isEmpty(auth)){
                    String url = auth.substring(auth.indexOf("url=") + 4,auth.indexOf("user=") - 1);
                    String user = auth.substring(auth.indexOf("user=") + 5,auth.indexOf("pswd=") - 1);
                    String pswd = auth.substring(auth.indexOf("pswd=") + 5);
                    JDBCConnectionInfo userInfo = new JDBCConnectionInfo(url,user,pswd);
                    connectionInfos.add(userInfo);
                }
            }
        }

        step = Integer.parseInt(properties.get("step"));
        pluginActivateType = PluginActivateType.valueOf(properties.get("pluginActivateType"));
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "oracle";
    }

    /**
     * 监控值的获取和上报周期(秒)
     *
     * @return
     */
    @Override
    public int step() {
        return step;
    }

    /**
     * 插件运行方式
     *
     * @return
     */
    @Override
    public PluginActivateType activateType() {
        return pluginActivateType;
    }

    /**
     * Agent关闭时的调用钩子
     * 如，可用于插件的资源释放等操作
     */
    @Override
    public void agentShutdownHook() {

    }
}
