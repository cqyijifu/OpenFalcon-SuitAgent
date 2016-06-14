package com.yiji.falcon.agent.plugins.oracle;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/17.
 */

import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.plugins.JDBCMetricsValue;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by QianLong on 16/5/17.
 */
public class OracleReportJob implements Job {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JDBCMetricsValue metricsValue = new OracleMetricsValue();
            ReportMetrics.push(metricsValue.getReportObjects());
        } catch (Exception e) {
            log.error("agent运行异常",e);
        }
    }
}
