/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.tomcat;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-23 16:09 创建
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.jmx.vo.JMXObjectNameInfo;
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.plugins.util.CacheUtil;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guqiu@yiji.com
 */
public class TomcatPlugin implements JMXPlugin {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String basePropertiesKey;
    private String jmxServerName;
    private int step;
    private PluginActivateType pluginActivateType;
    private static final ConcurrentHashMap<String,String> agentSignNameCache = new ConcurrentHashMap<>();

    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     *
     * @param properties 包含的配置:
     *                   1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     *                   2、插件指定的配置文件的全部配置信息(参见 {@link com.yiji.falcon.agent.plugins.Plugin#configFileName()} 接口项)
     *                   3、授权配置项(参见 {@link com.yiji.falcon.agent.plugins.Plugin#authorizationKeyPrefix()} 接口项
     */
    @Override
    public void init(Map<String, String> properties) {
        basePropertiesKey = properties.get("basePropertiesKey");
        jmxServerName = properties.get("jmxServerName");
        step = Integer.parseInt(properties.get("step"));
        pluginActivateType = PluginActivateType.valueOf(properties.get("pluginActivateType"));
    }


    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return 若无配置文件, 可返回null
     */
    @Override
    public String basePropertiesKey() {
        return basePropertiesKey;
    }

    /**
     * 该插件所要监控的服务在JMX连接中的displayName识别名
     * 若有该插件监控的相同类型服务,但是displayName不一样,可用逗号(,)进行分隔,进行统一监控
     *
     * @return
     */
    @Override
    public String jmxServerName() {
        return jmxServerName;
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "tomcat";
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

    /**
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 如该服务运行的端口号等
     * 若不需要指定则可返回null
     *
     * @param jmxMetricsValueInfo 该服务连接的jmx对象
     * @param pid                   该服务当前运行的进程id
     * @return
     */
    @Override
    public String agentSignName(JMXMetricsValueInfo jmxMetricsValueInfo, int pid) {

        try {
            //清除过期的缓存
            CacheUtil.getTimeoutCacheKeys(agentSignNameCache).forEach(agentSignNameCache::remove);
            String cacheKey = pid + "";
            String agentSignName = CacheUtil.getCacheValue(agentSignNameCache.get(cacheKey));
            if(StringUtils.isEmpty(agentSignName)){
                StringBuilder name = new StringBuilder();
                if(jmxMetricsValueInfo.getJmxObjectNameInfoList() != null){
                    for (JMXObjectNameInfo jmxObjectNameInfo : jmxMetricsValueInfo.getJmxObjectNameInfoList()) {
                        if(jmxObjectNameInfo.getObjectName().toString().contains("Catalina:type=Connector")){
                            for (Map.Entry<String, Object> entry : jmxObjectNameInfo.getMetricsValue().entrySet()) {
                                if("port".equals(entry.getKey())){
                                    String value = entry.getValue().toString();
                                    if ("".equals(name.toString())) {
                                        name.append(value);
                                    } else {
                                        name.append("-").append(value);
                                    }
                                }
                            }
                        }
                    }
                }

                String dirName = getServerDirName(pid);
                if(StringUtils.isEmpty(dirName)){
                    agentSignName = name.toString();
                }else {
                    agentSignName = name + "-" + dirName;
                    agentSignNameCache.put(cacheKey,CacheUtil.setCacheValue(agentSignName));
                }
            }

            return agentSignName;
        } catch (Exception e) {
            log.error("agentSignName获取失败", e);
            return "";
        }
    }

    private String getServerDirName(int pid) {
        String dirName = serverPath(pid, "");;
        if (dirName != null) {
            if(dirName.contains(File.separator)){
                dirName = dirName.substring(dirName.lastIndexOf(File.separator) + 1);
            }
        }
        return dirName;
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

    @Override
    public String serverPath(int pid, String serverName) {
        String dirPath = "";
        try {
            String cmd = "ps aux | grep " + pid;
            CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(cmd,false,7);
            String msg = executeResult.msg;
            String[] ss = msg.split("\\s+");
            for (String s1 : ss) {
                if(s1 != null && s1.contains("catalina.base=")){
                    dirPath = s1.split("=")[1];

                }
            }
        } catch (IOException e) {
            log.error("tomcat serverDirPath获取异常", e);
        }
        return dirPath;
    }

    @Override
    public String serverDirName(int pid) {
        return null;
    }
}
