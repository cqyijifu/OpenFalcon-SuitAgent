/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.job;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 11:00 创建
 */

import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import com.yiji.falcon.agent.plugins.metrics.DetectMetricsValue;
import com.yiji.falcon.agent.util.ExecuteThreadUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guqiu@yiji.com
 */
public class DetectPluginJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String pluginName = jobDataMap.getString("pluginName");
        try {
            DetectPlugin detectPlugin = (DetectPlugin) jobDataMap.get("pluginObject");
            MetricsCommon metricsValue = new DetectMetricsValue(detectPlugin);
            //可能会涉及到外网的连接,采用异步方式
            ExecuteThreadUtil.execute(new JobThread(metricsValue,"detect job thread"));
        } catch (Exception e) {
            logger.error("插件 {} 运行异常",pluginName,e);
        }
    }
}
