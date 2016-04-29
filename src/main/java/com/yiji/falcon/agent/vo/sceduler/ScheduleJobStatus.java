package com.yiji.falcon.agent.vo.sceduler;/**
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
 */

/**
 * 创建计划任务的结果返回值
 * Copyright 2014-2015 the original BZTWT
 * Created by QianLong on 2014/7/11 0011.
 */
public enum ScheduleJobStatus {

    /**
     * 任务创建成功
     */
    SUCCESS,
    /**
     * 已存在相同的任务(key)
     */
    ISEXIST,
    /**
     * 传入的对象条不满足任务的创建条件
     */
    DISCONTENT,
    /**
     * 创建任务失败
     */
    FAILED;

}

