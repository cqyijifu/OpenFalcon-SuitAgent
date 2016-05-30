package com.yiji.falcon.agent.plugins.logstash;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/30.
 */

import com.yiji.falcon.agent.common.ReportMetrics;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by QianLong on 16/5/30.
 */
public class LogstashReportJob implements Job {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LogstashMetricsValue metricsValue = new LogstashMetricsValue();
        try {
            ReportMetrics.push(metricsValue.getReportObjects());
        } catch (Exception e) {
            log.error("agent运行异常",e);
        }
    }
}
