/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.job;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 11:00 创建
 */

import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.metrics.DetectMetricsValue;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import com.yiji.falcon.agent.util.ExecuteThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class DetectPluginJob implements Job {
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
            log.error("插件 {} 运行异常",pluginName,e);
        }
    }
}
