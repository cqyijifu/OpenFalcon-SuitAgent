package com.yiji.falcon.agent.common;/**
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * 计划任务工厂类
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
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
