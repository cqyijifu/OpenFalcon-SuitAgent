package com.yiji.falcon.agent.common;/**
 * Copyright 2016-2017 the original ql
 * Created by QianLong on 16/6/14.
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.jmx.JMXConnection;
import com.yiji.falcon.agent.plugins.elasticSearch.ElasticSearchReportJob;
import com.yiji.falcon.agent.plugins.logstash.LogstashReportJob;
import com.yiji.falcon.agent.plugins.oracle.OracleReportJob;
import com.yiji.falcon.agent.plugins.tomcat.TomcatReportJob;
import com.yiji.falcon.agent.plugins.zk.ZKReportJob;
import com.yiji.falcon.agent.util.CronUtil;
import com.yiji.falcon.agent.util.SchedulerUtil;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobResult;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobStatus;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Agent Work Logic
 * Created by QianLong on 16/6/14.
 */
public class AgentWorkLogic {

    private static final Logger log = LoggerFactory.getLogger(AgentWorkLogic.class);

    //正在work的job记录
    private static final ConcurrentHashMap<Class,Date> worked = new ConcurrentHashMap<>();

    /**
     * 添加work记录
     * @param jobClazz
     */
    private static void addWorkJob(Class<? extends Job> jobClazz){
        if(jobClazz != null){
            worked.putIfAbsent(jobClazz, new Date());
        }
    }

    /**
     * 判断指定job是否已经work
     * @param jobClazz
     * @return
     */
    private static boolean isHasWorked(Class<? extends Job> jobClazz){
        return worked.get(jobClazz) != null;
    }

    /**
     * 进行一次服务自动发现work启动
     * @throws SchedulerException
     */
    public static void autoWorkLogic() throws SchedulerException {
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentZkWork(),ZKReportJob.class,"zookeeper",AgentConfiguration.INSTANCE.getZkJmxServerName());
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentTomcatWork(),TomcatReportJob.class,"tomcat",AgentConfiguration.INSTANCE.getTomcatJmxServerName());
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentElasticSearchWork(),ElasticSearchReportJob.class,"elasticSearch",AgentConfiguration.INSTANCE.getElasticSearchJmxServerName());
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentLogstashWork(),LogstashReportJob.class,"logstash",AgentConfiguration.INSTANCE.getLogstashJmxServerName());

    }

    /**
     * 配置启动的work启动逻辑
     */
    public static void confWorkLogic() throws SchedulerException {
        if(AgentConfiguration.INSTANCE.getAgentFlushTime() != 0 &&
                !isHasWorked(AgentFlushJob.class)){
            //开启服务自动发现
            JobDetail job = getJobDetail(AgentFlushJob.class,"AgentFlush","Agent监控服务自动发现定时刷新功能Job");

            Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getAgentFlushTime(),"AgentFlush","Agent监控服务自动发现定时刷新push调度任务");
            ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
            workResult(scheduleJobResult);
        }else{
            log.info("Agent监控服务自动发现定时刷新功能未开启");
        }

        if("true".equalsIgnoreCase(AgentConfiguration.INSTANCE.getAgentOracleWork()) &&
                !isHasWorked(OracleReportJob.class)){
            //开启Oracle
            JobDetail job = getJobDetail(OracleReportJob.class,"oracle","oracle的监控数据push调度JOB");

            Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getOracleStep(),"oracle","oracle的监控数据push调度任务");
            ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
            workResult(scheduleJobResult);
        }
    }

    /**
     * JMX服务的监控启动逻辑服务方法
     * @param workConf
     * @param jobClazz
     * @param desc
     * @param serverName
     * @throws SchedulerException
     */
    private static void autoWorkLogicForJMX(String workConf, Class<? extends Job> jobClazz, String desc, String serverName) throws SchedulerException {
        //只有指定job未启动过的情况下才进行work开启
        if(isHasWorked(jobClazz)){
            if("auto".equalsIgnoreCase(workConf)){
                if(JMXConnection.hasJMXServerInLocal(serverName)){
                    //开启服务监控
                    log.info("自动发现JMX服务:{}",serverName);
                    JobDetail job = getJobDetail(jobClazz,desc,desc + "的监控数据push调度JOB");
                    Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getZkStep(),desc,desc + "的监控数据push调度任务");
                    ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
                    workResult(scheduleJobResult);
                }
            }else if("true".equalsIgnoreCase(workConf)){
                JobDetail job = getJobDetail(jobClazz,desc,desc + "的监控数据push调度JOB");
                Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getZkStep(),desc,desc + "的监控数据push调度任务");
                ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
                workResult(scheduleJobResult);
            }
        }
    }

    /**
     * 启动结果处理并记录work
     * @param scheduleJobResult
     */
    public static void workResult(ScheduleJobResult scheduleJobResult){
        if(scheduleJobResult.getScheduleJobStatus() == ScheduleJobStatus.SUCCESS){
            log.info("{} 启动成功",scheduleJobResult.getTriggerKey().getName());
            //记录work
            addWorkJob(scheduleJobResult.getJobDetail().getJobClass());
        }else if(scheduleJobResult.getScheduleJobStatus() == ScheduleJobStatus.FAILED){
            log.error("{} 启动失败",scheduleJobResult.getTriggerKey().getName());
        }
    }

    /**
     * 获取计划任务JOB
     * @param job
     * @param id
     * @param description
     * @return
     */
    public static JobDetail getJobDetail(Class <? extends Job> job, String id, String description){
        return JobBuilder.newJob(job)
                .withIdentity(id + "-scheduler-job", "job-metricsScheduler")
                .withDescription(description)
                .build();
    }

    /**
     * 获取调度器
     * @param step
     * @param id
     * @param description
     * @return
     */
    public static Trigger getTrigger(int step, String id, String description){
        String cron = CronUtil.getCronBySecondScheduler(step);
        Trigger trigger = null;
        if(cron != null){
            log.info("启动{ " + description + " }调度:" + cron);
            trigger = newTrigger()
                    .withIdentity(id + "-agent-scheduler-trigger", "trigger-metricsScheduler")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .startNow()
                    .withDescription(description)
                    .build();
        }else{
            log.error("agent 启动失败. 调度时间配置失败");
            System.exit(0);
        }
        return trigger;
    }
}
