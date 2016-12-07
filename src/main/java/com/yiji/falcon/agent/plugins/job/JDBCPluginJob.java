/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.job;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-28 10:45 创建
 */

import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.plugins.JDBCPlugin;
import com.yiji.falcon.agent.plugins.metrics.JDBCMetricsValue;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class JDBCPluginJob implements Job{

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String pluginName = jobDataMap.getString("pluginName");
        try {
            JDBCPlugin jdbcPlugin = (JDBCPlugin) jobDataMap.get("pluginObject");
            MetricsCommon jdbcMetricsValue = new JDBCMetricsValue(jdbcPlugin);
            ReportMetrics.push(jdbcMetricsValue.getReportObjects());
        } catch (Exception e) {
            log.error("插件 {} 运行异常",pluginName,e);
        }
    }
}
