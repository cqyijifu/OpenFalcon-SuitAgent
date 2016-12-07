/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.job;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-13 11:27 创建
 */

import com.yiji.falcon.agent.plugins.SNMPV3Plugin;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import com.yiji.falcon.agent.plugins.metrics.SNMPV3MetricsValue;
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
public class SNMPPluginJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String pluginName = jobDataMap.getString("pluginName");
        try {
            SNMPV3Plugin plugin = (SNMPV3Plugin) jobDataMap.get("pluginObject");
            MetricsCommon metricsValue = new SNMPV3MetricsValue(plugin);
            //SNMP监控数据获取时间较长,采用异步方式
            ExecuteThreadUtil.execute(new JobThread(metricsValue,"snmp v3 job thread"));
        } catch (Exception e) {
            log.error("插件 {} 运行异常",pluginName,e);
        }
    }

}
