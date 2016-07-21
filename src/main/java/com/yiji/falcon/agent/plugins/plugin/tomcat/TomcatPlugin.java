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
import com.yiji.falcon.agent.util.CommendUtil;
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

import static com.yiji.falcon.agent.util.CommendUtil.exec;

/**
 * @author guqiu@yiji.com
 */
public class TomcatPlugin implements JMXPlugin {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
            return name;
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

    @Override
    public String serverDirName(int pid) {
        String dirName = "";
        try {
            String cmd = "lsof -p " + pid + " | grep catalina.jar";
            CommendUtil.ExecuteResult executeResult = exec(cmd);
            String msg = executeResult.msg;
            String[] ss = msg.split("\\s");
            for (String s : ss) {
                if(s.contains("catalina.jar")){
                    s = s.substring(0,s.lastIndexOf("/"));
                    s = s.substring(0,s.lastIndexOf("/"));
                    s = s.substring(s.lastIndexOf("/") + 1,s.length());
                    dirName = s;
                    break;
                }
            }
        } catch (IOException e) {
            log.error("tomcat serverDirName获取异常",e);
        }
        return dirName;
    }
}
