package com.yiji.falcon.agent;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;
import com.yiji.falcon.agent.jmx.JMXConnection;
import com.yiji.falcon.agent.plugins.elasticSearch.ElasticSearchReportJob;
import com.yiji.falcon.agent.plugins.logstash.LogstashReportJob;
import com.yiji.falcon.agent.plugins.oracle.OracleConnection;
import com.yiji.falcon.agent.plugins.oracle.OracleReportJob;
import com.yiji.falcon.agent.plugins.tomcat.TomcatReportJob;
import com.yiji.falcon.agent.util.CronUtil;
import com.yiji.falcon.agent.util.DateUtil;
import com.yiji.falcon.agent.util.SchedulerUtil;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobResult;
import com.yiji.falcon.agent.plugins.zk.ZKReportJob;
import com.yiji.falcon.agent.vo.sceduler.ScheduleJobStatus;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Collection;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * agent启动类
 * Created by QianLong on 16/4/25.
 */
public class Agent extends Thread{

    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;

    private static final Logger log = LoggerFactory.getLogger(Agent.class);

    private ServerSocketChannel serverSocketChannel;
    private static final Charset charset = Charset.forName("UTF-8");

    @Override
    public void run() {
        try {
            this.serverStart(AgentConfiguration.INSTANCE.getAgentPort());
        } catch (IOException e) {
            log.error("Agent启动失败",e);
        }
    }

    /**
     * 启动agent服务
     * @param port
     * @throws IOException
     */
    private void serverStart(int port) throws IOException {
        if(serverSocketChannel == null){
            // 创建对象
            log.info("开启服务");
            serverSocketChannel = ServerSocketChannel.open();
            // 使在相同主机上，关机此服务器后，再次启动依然绑定相同的端口
            serverSocketChannel.socket().setReuseAddress(true);
        }
        log.info("绑定端口" + port);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));

        work();

        //阻塞式方式进行客户端连接操作,连接成功后,单独启动线程进行客户端的读写操作
        for(;;){
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                log.info("客户端连接成功：" + socketChannel.toString());

                //启动线程,进行客户端的对话操作
                Thread talk = new Talk(socketChannel,this);
                talk.setName(socketChannel.toString());
                talk.start();
            } catch (IOException e) {
                break;
            }
        }

    }

    /**
     * 关闭服务器
     */
    void shutdown() throws IOException {
        log.info("正在关闭服务器");
        serverSocketChannel.close();
        log.info("------------进行调度器关闭处理-------------------");
        SchedulerFactory sf = DirectSchedulerFactory.getInstance();
        //获取所有的Scheduler
        Collection<Scheduler> schedulers;
        try {
            schedulers = sf.getAllSchedulers();
            for (Scheduler scheduler : schedulers) {
                try {
                    log.info("关闭调度器scheduler：" + scheduler.getSchedulerName());
                    scheduler.shutdown(true);
                } catch (SchedulerException e) {
                    log.warn("关闭调度器出现异常",e);
                    try {
                        log.warn("调度器scheduler：" + scheduler.getSchedulerName() + "\n状态：started:" + scheduler.isStarted() +
                                " shutdown:" + scheduler.isShutdown());
                    } catch (SchedulerException ignored) {
                    }
                }
            }
        } catch (SchedulerException e) {
            log.warn("获取Schedulers发生异常：" + e);
            log.error("获取Schedulers发生异常 " + e.getMessage());
        }
        log.info("调度器关闭成功");

        log.info("关闭JMX连接");
        JMXConnection.close();
        log.info("关闭数据库连接");
        OracleConnection.close();

        log.info("服务器关闭成功");
        System.exit(0);
    }

    /**
     * JMX服务的监控启动逻辑服务方法
     * @param workConf
     * @param jobClazz
     * @param desc
     * @param serverName
     * @throws SchedulerException
     */
    private void workLogicForJMX(String workConf,Class<? extends Job> jobClazz,String desc,String serverName) throws SchedulerException {
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

    private void work(){
        try {
            workLogicForJMX(AgentConfiguration.INSTANCE.getAgentZkWork(),ZKReportJob.class,"zookeeper",AgentConfiguration.INSTANCE.getZkJmxServerName());
            workLogicForJMX(AgentConfiguration.INSTANCE.getAgentTomcatWork(),TomcatReportJob.class,"tomcat",AgentConfiguration.INSTANCE.getTomcatJmxServerName());
            workLogicForJMX(AgentConfiguration.INSTANCE.getAgentElasticSearchWork(),ElasticSearchReportJob.class,"elasticSearch",AgentConfiguration.INSTANCE.getElasticSearchJmxServerName());
            workLogicForJMX(AgentConfiguration.INSTANCE.getAgentLogstashWork(),LogstashReportJob.class,"logstash",AgentConfiguration.INSTANCE.getLogstashJmxServerName());
            if("true".equalsIgnoreCase(AgentConfiguration.INSTANCE.getAgentOracleWork())){
                //开启Oracle
                JobDetail job = getJobDetail(OracleReportJob.class,"oracle","oracle的监控数据push调度JOB");

                Trigger trigger = getTrigger(AgentConfiguration.INSTANCE.getOracleStep(),"oracle","oracle的监控数据push调度任务");
                ScheduleJobResult scheduleJobResult = SchedulerUtil.executeScheduleJob(job,trigger);
                workResult(scheduleJobResult);
            }
        } catch (SchedulerException e) {
            log.error("Agent启动失败 : 调度任务启动失败",e);
            System.exit(0);
        }
    }

    private void workResult(ScheduleJobResult scheduleJobResult){
        if(scheduleJobResult.getScheduleJobStatus() == ScheduleJobStatus.SUCCESS){
            log.info("{} 启动成功",scheduleJobResult.getTriggerKey().getName());
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
    private JobDetail getJobDetail(Class <? extends Job> job,String id,String description){
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
    private Trigger getTrigger(int step,String id,String description){
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

    static String decode(ByteBuffer buffer){
        CharBuffer charBuffer = charset.decode(buffer);
        return charBuffer.toString();
    }

    static ByteBuffer encode(String str){
        return charset.encode(str);
    }

    public static void main(String[] args){
        String errorMsg = "Syntax: program < start | stop | status >";
        if(args.length < 1 ||
                        !("start".equals(args[0]) ||
                                "stop".equals(args[0]) ||
                                "status".equals(args[0])
                        ))
        {
            ERR.println(errorMsg);
            return;
        }

        switch (args[0]){
            case "start":
                //自定义日志配置文件
                PropertyConfigurator.configure(AgentConfiguration.INSTANCE.getLog4JConfPath());
                Thread main = new Agent();
                main.setName("FalconAgent");
                main.start();
                break;
            case "stop":
                try {
                    Client client = new Client();
                    client.start(AgentConfiguration.INSTANCE.getAgentPort());
                    client.sendCloseCommend();
                    client.talk();
                } catch (IOException e) {
                    OUT.println("Agent Not Start");
                }
                break;
            case "status":
                try {
                    Client client = new Client();
                    client.start(AgentConfiguration.INSTANCE.getAgentPort());
                    OUT.println("Agent Started On Port " + AgentConfiguration.INSTANCE.getAgentPort());
                    client.close();
                } catch (IOException e) {
                    OUT.println("Agent Not Start");
                }
                break;
            default:
                OUT.println(errorMsg);
        }

    }

}
