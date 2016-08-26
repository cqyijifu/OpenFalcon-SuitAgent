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
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.util.CommandUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guqiu@yiji.com
 */
public class TomcatPlugin implements JMXPlugin {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private ConcurrentHashMap<String,String> serverDirNameCatch = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,String> serverDirPathCatch = new ConcurrentHashMap<>();

    private String basePropertiesKey;
    private String jmxServerName;
    private int step;
    private PluginActivateType pluginActivateType;

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
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 如该服务运行的端口号等
     * 若不需要指定则可返回null
     *
     * @param mBeanServerConnection 该服务连接的mBeanServerConnection对象
     * @param pid                   该服务当前运行的进程id
     * @return
     */
    @Override
    public String agentSignName(MBeanServerConnection mBeanServerConnection, int pid) {
        try {
            String name = "";
            Set<ObjectInstance> beanSet = mBeanServerConnection.queryMBeans(null, null);
            for (ObjectInstance mbean : beanSet) {
                ObjectName objectName = mbean.getObjectName();
                if(objectName.toString().contains("Catalina:type=Connector")){
                    for (MBeanAttributeInfo mBeanAttributeInfo : mBeanServerConnection.getMBeanInfo(objectName).getAttributes()) {
                        String key = mBeanAttributeInfo.getName();
                        if("port".equals(key)){
                            String value = mBeanServerConnection.getAttribute(mbean.getObjectName(),key).toString();
                            if("".equals(name)){
                                name += value;
                            }else{
                                name += "-" + value;
                            }
                        }
                    }
                }
            }
            return StringUtils.isEmpty(name) ? serverDirName(pid) : name + "-" +serverDirName(pid);
        } catch (Exception e) {
            log.error("设置JMX name 失败",e);
            return "";
        }
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
        serverDirPathCatch.remove(StringUtils.getStringByInt(pid));
        serverDirNameCatch.remove(StringUtils.getStringByInt(pid));
    }

    /**
     * JMX服务的目录路径
     * 若实现此方法,则若该JMX连接不可用时,将会检查该JMX服务的目录是否存在,若不存在,将会清除此连接,并不再监控此JMX。
     * 否则,若JMX连接不可用,将会上报不可用的报告,且不会清除
     *
     * @param pid 服务的进程id
     * @return
     */
    @Override
    public String serverDirPath(int pid) {
        String key = StringUtils.getStringByInt(pid);
        String dirPath = serverDirPathCatch.get(key);
        if(dirPath == null){
            try {
                dirPath = CommandUtil.getCmdDirByPid(pid);
                if (dirPath != null) {
                    serverDirPathCatch.put(key,dirPath);
                }
            } catch (IOException e) {
                log.error("tomcat serverDirPath获取异常",e);
            }
        }
        return dirPath;
    }

    @Override
    public String serverDirName(int pid) {
        String key = StringUtils.getStringByInt(pid);
        String dirName = serverDirNameCatch.get(key);
        if(dirName == null){
            String dirPath = serverDirPath(pid);
            if(dirPath != null){
                dirName = dirPath.replace("/bin","");
                dirName = dirName.substring(dirName.lastIndexOf("/") + 1);
                serverDirNameCatch.put(key,dirName);
            }
        }
        return dirName;
    }
}
