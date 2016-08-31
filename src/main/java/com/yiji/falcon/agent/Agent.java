/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.jmx.JMXConnection;
import com.yiji.falcon.agent.plugins.metrics.SNMPV3MetricsValue;
import com.yiji.falcon.agent.plugins.util.PluginExecute;
import com.yiji.falcon.agent.plugins.util.PluginLibraryHelper;
import com.yiji.falcon.agent.util.*;
import com.yiji.falcon.agent.vo.HttpResult;
import com.yiji.falcon.agent.watcher.ConfDirWatcher;
import com.yiji.falcon.agent.watcher.PluginPropertiesWatcher;
import com.yiji.falcon.agent.web.HttpServer;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.DirectSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * agent服务
 * @author guqiu@yiji.com
 */
public class Agent extends Thread{

    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;
    public static int falconAgentPid = 0;

    private static final Logger log = LoggerFactory.getLogger(Agent.class);

    private ServerSocketChannel serverSocketChannel;
    private HttpServer httpServer = null;
    private static final Charset charset = Charset.forName("UTF-8");

    @Override
    public void run() {

        //Agent主服务添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                stopAgent();
            }
        });

        try {
            this.agentWebServerStart(AgentConfiguration.INSTANCE.getAgentWebPort());
            this.agentServerStart(AgentConfiguration.INSTANCE.getAgentPort());
        } catch (IOException e) {
            log.error("Agent启动失败",e);
        }
    }

    /**
     * 启动web服务
     * @param port
     */
    private void agentWebServerStart(int port){
        if(AgentConfiguration.INSTANCE.isWebEnable() && HttpServer.status == 0){
            httpServer = new HttpServer(port);
            httpServer.setName("agent web server thread");
            httpServer.start();
        }
    }

    /**
     * 启动agent服务
     * @param port
     * @throws IOException
     */
    private void agentServerStart(int port) throws IOException {
        String agentPushUrl = AgentConfiguration.INSTANCE.getAgentPushUrl();
        if(agentPushUrl.contains("127.0.0.1") ||
                agentPushUrl.contains("localhost") ||
                agentPushUrl.contains("0.0.0.0")){
            log.info("开始启动 Falcon Agent服务");
            String falconAgentConfFileName = "agent.cfg.json";
            String falconAgentConfFile = AgentConfiguration.INSTANCE.getFalconConfDir() + File.separator + falconAgentConfFileName;
            if(!FileUtil.isExist(falconAgentConfFile)){
                log.error("Agent 启动失败 - Falcon Agent配置文件:{} 在目录:{} 下未找到,请确定配置文件是否正确配置",falconAgentConfFileName,AgentConfiguration.INSTANCE.getFalconConfDir());
                System.exit(0);
            }
            String falconAgentConfContent = FileUtil.getTextFileContent(falconAgentConfFile);
            if(StringUtils.isEmpty(falconAgentConfContent)){
                log.error("Agent 启动失败 - Falcon Agent配置文件:{} 无配置内容",falconAgentConfFile);
                System.exit(0);
            }
            String falconAgentDir = AgentConfiguration.INSTANCE.getFalconDir() + File.separator + "agent";
            if(FileUtil.writeTextToTextFile(falconAgentConfContent,falconAgentDir,"cfg.json",false)){
                String common = falconAgentDir + File.separator + "control start";
                CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithTimeOut(common,10, TimeUnit.SECONDS);
                log.info("正在启动 Falcon Agent : {}",executeResult.msg);
                String msg = executeResult.msg.trim();
                if(msg.contains("falcon-agent started")){
                    falconAgentPid = Integer.parseInt(msg.substring(
                            msg.indexOf("pid=") + 4
                    ));
                    log.info("Falcon Agent 启动成功,进程ID为 : {}",falconAgentPid);
                }else if(msg.contains("falcon-agent now is running already")){
                    falconAgentPid = Integer.parseInt(msg.substring(
                            msg.indexOf("pid=") + 4
                    ));
                    log.info("Falcon Agent 已启动,无需重复启动,进程ID为 : {}",falconAgentPid);
                }else{
                    log.error("Agent启动失败 - Falcon Agent 启动失败");
                    System.exit(0);
                }
            }else{
                log.error("Agent启动失败 - Falcon Agent配置文件写入失败,请检查文件权限");
                System.exit(0);
            }
        }

        if(serverSocketChannel == null){
            // 创建对象
            log.info("正在启动Agent服务");
            serverSocketChannel = ServerSocketChannel.open();
            // 使在相同主机上，关机此服务器后，再次启动依然绑定相同的端口
            serverSocketChannel.socket().setReuseAddress(true);
        }
        log.info("Agent绑定端口:" + port);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log.error("线程 {} 未处理的异常",t.getName(),e);
        });

        work();

        //阻塞式方式进行客户端连接操作,连接成功后,单独启动线程进行客户端的读写操作
        for(;;){
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();

                ByteBuffer buffer = ByteBuffer.allocate(27);
                socketChannel.read(buffer);
                String receive = new String(buffer.array());
                if("I am is Falcon Agent Client".equals(receive)){
                    //启动线程,进行客户端的对话操作
                    log.info("客户端连接成功：" + socketChannel.toString());
                    Thread talk = new Talk(socketChannel,this);
                    talk.setName(socketChannel.toString());
                    talk.start();
                }else{
                    //不是Agent Client 关闭连接
                    socketChannel.close();
                }

            } catch (IOException e) {
                break;
            }
        }

    }

    /**
     * 关闭服务器
     */
    void shutdown() {
        log.info("正在关闭服务器");
        if(httpServer != null){
            try {
                log.info("发送web关闭命令");
                HttpResult response = HttpUtil.get(String.format("http://127.0.0.1:%d/__SHUTDOWN__",AgentConfiguration.INSTANCE.getAgentWebPort()));
                log.info("web关闭结果:{}",response);
            } catch (IOException e) {
                log.error("web关闭异常",e);
            }
        }
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            log.error("serverSocketChannel.close()异常",e);
        }
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
        log.info("关闭SNMP连接");
        SNMPV3MetricsValue.closeAllSession();

        String agentPushUrl = AgentConfiguration.INSTANCE.getAgentPushUrl();
        if(agentPushUrl.contains("127.0.0.1") ||
                agentPushUrl.contains("localhost") ||
                agentPushUrl.contains("0.0.0.0")){
            try {
                String falconAgentDir = AgentConfiguration.INSTANCE.getFalconDir() + File.separator + "agent";
                String common = falconAgentDir + File.separator + "control stop";
                CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithTimeOut(common,10,TimeUnit.SECONDS);
                if(executeResult.isSuccess){
                    log.info("正在关闭 Falcon Agent : {}",executeResult.msg);
                    if(executeResult.msg.contains("falcon-agent stoped")){
                        log.info("Falcon Agent 关闭成功");
                    }
                }else{
                    log.error("Falcon Agent 服务自动关闭失败,请手动关闭,Falcon Agent 进程ID为 : {}",falconAgentPid);
                }
            } catch (IOException e) {
                log.error("Falcon Agent 自动关闭失败,请手动关闭,Falcon Agent 进程ID为 : {}",falconAgentPid,e);
            }
        }

        log.info("关闭线程池");
        ExecuteThreadUtil.shutdown();

        log.info("服务器关闭成功");
        System.exit(0);
    }

    /**
     * 监控服务启动
     */
    private void work(){
        try {
            //注册插件
            new PluginLibraryHelper().register();
            //运行插件
            PluginExecute.start();
            //启动配置文件监听
            Thread pluginWatcher = new PluginPropertiesWatcher(AgentConfiguration.INSTANCE.getPluginConfPath());
            pluginWatcher.setName("pluginDirWatcher");
            pluginWatcher.setDaemon(true);
            Thread confWatcher = new ConfDirWatcher(AgentConfiguration.INSTANCE.getAgentConfPath().substring(0,AgentConfiguration.INSTANCE.getAgentConfPath().lastIndexOf("/")));
            confWatcher.setName("confDirWatcher");
            confWatcher.setDaemon(true);
            pluginWatcher.start();
            confWatcher.start();
        } catch (Exception e) {
            log.error("Agent启动失败",e);
            System.exit(0);
        }
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
                stopAgent();
                break;
            case "status":
                statusAgent();
                break;
            default:
                OUT.println(errorMsg);
        }

    }

    private static void statusAgent(){
        try {
            Client client = new Client();
            client.start(AgentConfiguration.INSTANCE.getAgentPort());
            OUT.println("Agent Started On Port " + AgentConfiguration.INSTANCE.getAgentPort());
            client.closeClient();
        } catch (IOException e) {
            exception(e);
            OUT.println("Connection refused ! Agent Not Start");
        }
    }

    private static void stopAgent(){
        try {
            Client client = new Client();
            client.start(AgentConfiguration.INSTANCE.getAgentPort());
            client.sendCloseCommend();
            client.talk();
        } catch (IOException e) {
            exception(e);
            OUT.println("Connection refused ! Agent Not Start");
        }
    }

    private static void exception(Exception e){
        if(e instanceof java.net.ConnectException &&
                "Connection refused".equals(e.getMessage())){
            return;
        }
        e.printStackTrace();
    }

}
