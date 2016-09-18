/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.standalone;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-27 16:13 创建
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author guqiu@yiji.com
 */
public class StandaloneJarPlugin implements JMXPlugin {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConcurrentHashMap<String,String> serverDirPathCatch = new ConcurrentHashMap<>();
    private String jmxServerDir;
    private String jmxServerName;
    private int step;
    private PluginActivateType pluginActivateType;

    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return 若无配置文件, 可返回null
     */
    @Override
    public String basePropertiesKey() {
        return null;
    }

    /**
     * 该插件所要监控的服务在JMX连接中的displayName识别名
     * 若有该插件监控的相同类型服务,但是displayName不一样,可用逗号(,)进行分隔,进行统一监控
     *
     * @return
     */
    @Override
    public String jmxServerName() {
        StringBuilder sb = new StringBuilder();
        if(!StringUtils.isEmpty(jmxServerDir)){
            for (String dir : jmxServerDir.split(",")) {
                if(!StringUtils.isEmpty(dir)){
                    Path path = Paths.get(dir);
                    try {
                        Files.walkFileTree(path,new SimpleFileVisitor<Path>(){
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                String fileName = file.getFileName().toString();
                                String fineNameLower = fileName.toLowerCase();
                                if(!fineNameLower.contains("-sources") && fineNameLower.endsWith(".jar")){
                                    sb.append(",").append(fileName);
                                }

                                return super.visitFile(file, attrs);
                            }
                        });
                    } catch (IOException e) {
                        logger.error("遍历目录 {} 发生异常",jmxServerDir);
                    }
                }
            }
        }
        sb.append(jmxServerName == null ? "" : "," + jmxServerName);
        return sb.toString();
    }

    /**
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 可用变量:
     * {jmxServerName} - 代表直接使用当前服务的jmxServerName
     * 如该服务运行的端口号等
     * 若不需要指定则可返回null
     *
     * @param mBeanServerConnection 该服务连接的mBeanServerConnection对象
     * @param pid                   该服务当前运行的进程id
     * @return
     */
    @Override
    public String agentSignName(MBeanServerConnection mBeanServerConnection, int pid) {
        return "{jmxServerName}";
    }

    /**
     * 插件监控的服务正常运行时的內建监控报告
     * 若有些特殊的监控值无法用配置文件进行配置监控,可利用此方法进行硬编码形式进行获取
     * 注:此方法只有在监控对象可用时,才会调用,并加入到监控值报告中,一并上传
     *
     * @param metricsValueInfo 当前的JMXMetricsValueInfo信息
     * @return
     */
    @Override
    public Collection<FalconReportObject> inbuiltReportObjectsForValid(JMXMetricsValueInfo metricsValueInfo) {
        return new ArrayList<>();
    }

    /**
     * 当JMX连接的应用已下线(此链接的目标目录已不存在)时,将会在清除连接时,调用此方法进行相关资源的释放操作
     * 该操作有具体的插件自己实现
     *
     * @param pid
     */
    @Override
    public void releaseOption(int pid) {

    }

    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     * @param properties
     * 包含的配置:
     * 1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     * 2、插件指定的配置文件的全部配置信息(参见 {@link com.yiji.falcon.agent.plugins.Plugin#configFileName()} 接口项)
     * 3、授权配置项(参见 {@link com.yiji.falcon.agent.plugins.Plugin#authorizationKeyPrefix()} 接口项
     */
    @Override
    public void init(Map<String, String> properties) {
        jmxServerDir = properties.get("jmxServerDir");
        jmxServerName = properties.get("jmxServerName");
        step = Integer.parseInt(properties.get("step"));
        pluginActivateType = PluginActivateType.valueOf(properties.get("pluginActivateType"));
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "standaloneJar";
    }

    /**
     * 监控值的获取和上报周期(秒)
     *
     * @return
     */
    @Override
    public int step() {
        return step;
    }

    /**
     * 插件运行方式
     *
     * @return
     */
    @Override
    public PluginActivateType activateType() {
        return pluginActivateType;
    }

    /**
     * Agent关闭时的调用钩子
     * 如，可用于插件的资源释放等操作
     */
    @Override
    public void agentShutdownHook() {

    }

    @Override
    public String serverPath(int pid, String serverName) {
        String key = StringUtils.getStringByInt(pid);
        String dirPath = serverDirPathCatch.get(key);
        if(StringUtils.isEmpty(dirPath)){
            try {
                String cmd = "lsof -p " + pid + " | grep " + serverName;
                CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(cmd,false,10, TimeUnit.SECONDS);
                String msg = executeResult.msg;
                String[] ss = msg.split("\n");
                for (String s : ss) {
                    if(!StringUtils.isEmpty(s)){
                        String[] split = s.split("\\s+");
                        dirPath = split[split.length - 1];
                        break;
                    }
                }

                if (dirPath != null) {
                    dirPath += File.separator + serverName;
                    serverDirPathCatch.put(key,dirPath);
                }
            } catch (IOException e) {
                logger.error("standaloneJar serverDirPath获取异常",e);
            }
        }
        return dirPath;
    }
}
