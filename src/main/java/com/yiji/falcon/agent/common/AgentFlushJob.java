package com.yiji.falcon.agent.common;/**
 * Copyright 2016-2017 the original ql
 * Created by QianLong on 16/6/14.
 */

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yiji.falcon.agent.common.AgentWorkLogic.autoWorkLogic;

/**
 * agent自动发现服务JOB
 * Created by QianLong on 16/6/14.
 */
public class AgentFlushJob implements Job{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("开始自动扫描监控服务");
            autoWorkLogic();
        } catch (Exception e) {
            log.error("agent运行异常",e);
        }
    }
}
