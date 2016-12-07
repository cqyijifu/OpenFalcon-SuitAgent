/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import com.yiji.falcon.agent.common.SchedulerFactory;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobResult;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 计划任务辅助工具类
 * @author guqiu@yiji.com
 */
@Slf4j
public class SchedulerUtil {

    /**
     * 执行计划任务
     * @param job
     * @param trigger
     * @return
     * @throws SchedulerException
     */
    public static ScheduleJobResult executeScheduleJob(JobDetail job, Trigger trigger) throws SchedulerException {
        ScheduleJobResult scheduleJobResult = new ScheduleJobResult();
        //判断是否满足计划任务的创建条件
        if(job.getKey() == null || trigger.getKey() == null || job.getJobDataMap() == null){
            scheduleJobResult.setScheduleJobStatus(ScheduleJobStatus.FAILED);
            //不满足计划任务的创建条件，返回scheduleJobResult值类
            return scheduleJobResult;
        }
        scheduleJobResult.setJobDetail(job);
        scheduleJobResult.setTrigger(trigger);
        //开始分配计划任务
        Scheduler scheduler  = SchedulerFactory.getScheduler();
        //开始判断是否存在相同的计划任务
        if(scheduler.checkExists(job.getKey())){
            log.info("存在相同的计划任务:{}",job.getKey());
            scheduler.deleteJob(job.getKey());
            scheduleJobResult.setJobKey(job.getKey());
            scheduleJobResult.setTriggerKey(trigger.getKey());
            scheduleJobResult.setScheduleJobStatus(ScheduleJobStatus.ISEXIST);
            scheduler.scheduleJob(job,trigger);
            scheduler.start();
        }else{
            scheduler.scheduleJob(job,trigger);
            scheduler.start();
            scheduleJobResult.setJobKey(job.getKey());
            scheduleJobResult.setTriggerKey(trigger.getKey());
            scheduleJobResult.setScheduleJobStatus(ScheduleJobStatus.SUCCESS);
        }
        //计划任务分配成功
        return scheduleJobResult;
    }

}
