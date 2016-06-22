/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.common;

import com.yiji.falcon.agent.config.AgentConfiguration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class SchedulerFactory {

    /**
     * 获取调度器
     * @return
     * @throws SchedulerException
     * 调度器获取失败异常
     */
    public static Scheduler getScheduler() throws SchedulerException {
        org.quartz.SchedulerFactory sf = new StdSchedulerFactory(AgentConfiguration.INSTANCE.getQuartzConfPath());
        return sf.getScheduler();
    }

}
