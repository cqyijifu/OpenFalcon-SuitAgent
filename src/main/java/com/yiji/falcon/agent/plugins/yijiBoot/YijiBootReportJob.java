package com.yiji.falcon.agent.plugins.yijiBoot;/**
 * Copyright 2016-2017 the original ql
 * Created by QianLong on 16/6/15.
 */

import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.util.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by QianLong on 16/6/15.
 */
public class YijiBootReportJob implements Job {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String appJarName = context.getJobDetail().getJobDataMap().getString("appJarName");
            YijiBootMetricsValue metricValue = new YijiBootMetricsValue(appJarName);
            ReportMetrics.push(metricValue.getReportObjects());
        } catch (Exception e) {
            log.error("agent运行异常",e);
        }
    }
}
