package com.yiji.falcon.agent.zk;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/26.
 */

import com.yiji.falcon.agent.common.ReportMetrics;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import java.io.IOException;

/**
 * Created by QianLong on 16/4/26.
 */
public class ReportJob implements Job {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ZkMetricValue zkMetricValue = new ZkMetricValue();
        try {
            ReportMetrics.push(zkMetricValue.getConfReportObjects());
        } catch (IntrospectionException | ReflectionException | IOException | InstanceNotFoundException e) {
            log.error("agent运行异常",e);
        }
    }
}
