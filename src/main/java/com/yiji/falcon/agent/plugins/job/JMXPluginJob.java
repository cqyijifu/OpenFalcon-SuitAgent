/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.job;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-24 13:42 创建
 */

import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.plugins.metrics.JMXMetricsValue;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class JMXPluginJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String pluginName = jobDataMap.getString("pluginName");
        try {
            JMXPlugin jmxPlugin = (JMXPlugin) jobDataMap.get("pluginObject");
            String jmxServerName = jobDataMap.getString("jmxServerName");

            List<JMXMetricsValueInfo> jmxMetricsValueInfos = JMXManager.getJmxMetricValue(jmxServerName,jmxPlugin);

            //设置agentSignName
            for (JMXMetricsValueInfo jmxMetricsValueInfo : jmxMetricsValueInfos) {
                String agentSignName = jmxPlugin.agentSignName(jmxMetricsValueInfo,
                        jmxMetricsValueInfo.getJmxConnectionInfo().getPid());
                if ("{jmxServerName}".equals(agentSignName)) {
                    //设置变量
                    jmxMetricsValueInfo.getJmxConnectionInfo().setName(jmxServerName);
                }else{
                    jmxMetricsValueInfo.getJmxConnectionInfo().setName(agentSignName);
                }
            }

            MetricsCommon jmxMetricsValue = new JMXMetricsValue(jmxPlugin,jmxMetricsValueInfos);
            ReportMetrics.push(jmxMetricsValue.getReportObjects());
        } catch (Exception e) {
            log.error("插件 {} 运行异常",pluginName,e);
        }
    }
}
