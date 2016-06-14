package com.yiji.falcon.agent.util;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/26.
 */

import com.yiji.falcon.agent.common.SchedulerFactory;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobResult;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobStatus;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计划任务辅助工具类
 * Created by QianLong on 16/4/26.
 */
public class SchedulerUtil {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerUtil.class);

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
            logger.info("存在相同的计划任务:{}",job.getKey());
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
