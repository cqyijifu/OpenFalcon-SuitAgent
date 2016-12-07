package com.yiji.falcon.agent.vo.sceduler;/**
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
 */

import lombok.Data;
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
@Data
public class ScheduleJobResult implements Serializable {

    private TriggerKey triggerKey;
    private JobKey jobKey;
    private JobDetail jobDetail;
    private Trigger trigger;
    private ScheduleJobStatus scheduleJobStatus;

}
