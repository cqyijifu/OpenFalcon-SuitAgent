/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.common;

import com.yiji.falcon.agent.plugins.PluginExecute;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * agent自动发现服务JOB
 * @author guqiu@yiji.com
 */
public class AgentFlushJob implements Job{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("开始自动扫描插件服务");
            PluginExecute.run();
        } catch (Exception e) {
            log.error("agent运行异常",e);
        }
    }
}
