package com.yiji.falcon.agent.vo.sceduler;/**
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
 */

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.io.Serializable;

/**
 * 新建计划任务结果返回值类
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
 */
public class ScheduleJobResult implements Serializable {

    private TriggerKey triggerKey;
    private JobKey jobKey;
    private JobDetail jobDetail;
    private Trigger trigger;
    private ScheduleJobStatus scheduleJobStatus;

    @Override
    public String toString() {
        return "ScheduleJobResult{" +
                "triggerKey=" + triggerKey +
                ", jobKey=" + jobKey +
                ", jobDetail=" + jobDetail +
                ", trigger=" + trigger +
                ", scheduleJobStatus=" + scheduleJobStatus +
                '}';
    }

    public TriggerKey getTriggerKey() {
        return triggerKey;
    }

    public void setTriggerKey(TriggerKey triggerKey) {
        this.triggerKey = triggerKey;
    }

    public JobKey getJobKey() {

        return jobKey;
    }

    public void setJobKey(JobKey jobKey) {
        this.jobKey = jobKey;
    }

    public ScheduleJobStatus getScheduleJobStatus() {
        return scheduleJobStatus;
    }

    public void setScheduleJobStatus(ScheduleJobStatus scheduleJobStatus) {
        this.scheduleJobStatus = scheduleJobStatus;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
