package com.yiji.falcon.agent.plugins.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/26.
 */

import com.yiji.falcon.agent.common.ReportMetrics;
import com.yiji.falcon.agent.plugins.JMXMetricsValue;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by QianLong on 16/4/26.
 */
public class ZKReportJob implements Job {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JMXMetricsValue zkMetricValue = new ZkMetricValue();
        try {
            ReportMetrics.push(zkMetricValue.getReportObjects());
        } catch (Exception e) {
            log.error("agent运行异常",e);
        }
    }
}
