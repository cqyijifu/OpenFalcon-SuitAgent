/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.common;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.jmx.JMXConnection;
import com.yiji.falcon.agent.plugins.elasticSearch.ElasticSearchReportJob;
import com.yiji.falcon.agent.plugins.logstash.LogstashReportJob;
import com.yiji.falcon.agent.plugins.oracle.OracleReportJob;
import com.yiji.falcon.agent.plugins.tomcat.TomcatReportJob;
import com.yiji.falcon.agent.plugins.yijiBoot.YijiBootReportJob;
import com.yiji.falcon.agent.plugins.zk.ZKReportJob;
import com.yiji.falcon.agent.util.CronUtil;
import com.yiji.falcon.agent.util.SchedulerUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobResult;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobStatus;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentSkipListSet;

import static org.quartz.TriggerBuilder.newTrigger;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class AgentWorkLogic {

    private static final Logger log = LoggerFactory.getLogger(AgentWorkLogic.class);

    //正在work的job记录
    private static final ConcurrentSkipListSet<String> worked = new ConcurrentSkipListSet<>();

    /**
     * 添加work记录
     * @param serverName
     */
    private static void addWorkJob(String serverName){
        if(!StringUtils.isEmpty(serverName)){
            worked.add(serverName);
        }
    }

    /**
     * 判断指定job是否已经work
     * @param serverName
     * @return
     */
    private static boolean isHasWorked(String serverName){
        return worked.contains(serverName);
    }

    /**
     * 进行一次服务自动发现work启动
     * @throws SchedulerException
     */
    public static void autoWorkLogic() throws SchedulerException {
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentZkWork(),ZKReportJob.class,"zookeeper",AgentConfiguration.INSTANCE.getZkJmxServerName(),new JobDataMap());
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentTomcatWork(),TomcatReportJob.class,"tomcat",AgentConfiguration.INSTANCE.getTomcatJmxServerName(),new JobDataMap());
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentElasticSearchWork(),ElasticSearchReportJob.class,"elasticSearch",AgentConfiguration.INSTANCE.getElasticSearchJmxServerName(),new JobDataMap());
        autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentLogstashWork(),LogstashReportJob.class,"logstash",AgentConfiguration.INSTANCE.getLogstashJmxServerName(),new JobDataMap());

        for (String serverName : AgentConfiguration.INSTANCE.getYijiBootJmxServerName()) {
            //分别启动不同的yijiBoot应用
            if(!StringUtils.isEmpty(serverName)){
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("appJarName",serverName);
                autoWorkLogicForJMX(AgentConfiguration.INSTANCE.getAgentYijiBootWork(),YijiBootReportJob.class,"yijiBoot-" + serverName,serverName,jobDataMap);
            }
        }

    }

    /**
     * 配置启动的work启动逻辑
     */
    public static void confWorkLogic() throws SchedulerException {

        String agentFlush = "AgentFlush";
        if(AgentConfiguration.INSTANCE.getAgentFlushTime() != 0 &&
                !isHasWorked(agentFlush)){
            //开启服务自动发现
            JobDetail job = getJobDetail(AgentFlushJob.class,agentFlush,"Agent监控服务自动发现定时刷新功能Job",new JobDataMap());

            Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getAgentFlushTime(),agentFlush,"Agent监控服务自动发现定时刷新push调度任务");
            ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
            workResult(scheduleJobResult,agentFlush);
        }else{
            log.info("Agent监控服务自动发现定时刷新功能未开启");
        }

        String oracle = "oracle";
        if("true".equalsIgnoreCase(AgentConfiguration.INSTANCE.getAgentOracleWork()) &&
                !isHasWorked(oracle)){
            //开启Oracle
            JobDetail job = getJobDetail(OracleReportJob.class,oracle, oracle +"的监控数据push调度JOB",new JobDataMap());

            Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getOracleStep(),oracle, oracle + "的监控数据push调度任务");
            ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
            workResult(scheduleJobResult,oracle);
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
    private static void autoWorkLogicForJMX(String workConf, Class<? extends Job> jobClazz, String desc, String serverName,JobDataMap jobDataMap) throws SchedulerException {
        //只有指定job未启动过的情况下才进行work开启
        if(!isHasWorked(serverName)){
            if("auto".equalsIgnoreCase(workConf)){
                if(JMXConnection.hasJMXServerInLocal(serverName)){
                    //开启服务监控
                    log.info("自动发现JMX服务:{}",serverName);
                    JobDetail job = getJobDetail(jobClazz,desc,desc + "的监控数据push调度JOB",jobDataMap);
                    Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getZkStep(),desc,desc + "的监控数据push调度任务");
                    ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
                    workResult(scheduleJobResult,serverName);
                }
            }else if("true".equalsIgnoreCase(workConf)){
                JobDetail job = getJobDetail(jobClazz,desc,desc + "的监控数据push调度JOB",jobDataMap);
                Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getZkStep(),desc,desc + "的监控数据push调度任务");
                ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
                workResult(scheduleJobResult,serverName);
            }
        }
    }

    /**
     * 启动结果处理并记录work
     * @param scheduleJobResult
     */
    public static void workResult(ScheduleJobResult scheduleJobResult,String serverName){
        if(scheduleJobResult.getScheduleJobStatus() == ScheduleJobStatus.SUCCESS){
            log.info("{} 启动成功",scheduleJobResult.getTriggerKey().getName());
            //记录work
            addWorkJob(serverName);
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
    public static JobDetail getJobDetail(Class <? extends Job> job, String id, String description,JobDataMap jobDataMap){
        return JobBuilder.newJob(job)
                .withIdentity(id + "-scheduler-job", "job-metricsScheduler")
                .withDescription(description)
                .setJobData(jobDataMap)
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
