/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.elasticSearch;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-27 18:03 创建
 */

import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import com.yiji.falcon.agent.plugins.util.PluginActivateType;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.HttpUtil;
import com.yiji.falcon.agent.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.yiji.falcon.agent.plugins.metrics.MetricsCommon.executeJsExpress;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class ElasticSearchPlugin implements JMXPlugin {

    private String basePropertiesKey;
    private String jmxServerName;
    private int step;
    private PluginActivateType pluginActivateType;
    private String pluginDir;
    private final static String metricsConfFile = "elasticSearchMetricsConf.yml";
    private static String lastAgentSignName = "";

    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return 若无配置文件, 可返回null
     */
    @Override
    public String basePropertiesKey() {
        return this.basePropertiesKey;
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
     * 该插件监控的服务标记名称,目的是为能够在操作系统中准确定位该插件监控的是哪个具体服务
     * 可用变量:
     * {jmxServerName} - 代表直接使用当前服务的jmxServerName
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
            lastAgentSignName = String.format("%d", ElasticSearchConfig.getHttpPort(pid));
            return lastAgentSignName;
        } catch (IOException e) {
            log.error("获取elasticSearch的名称异常,返回最后的AgentSignName：{}",lastAgentSignName);
            return lastAgentSignName;
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
        // 指定配置中配置的监控值
        int pid = metricsValueInfo.getJmxConnectionInfo().getPid();
        Set<FalconReportObject> result = new HashSet<>();
        String configPath = pluginDir + File.separator + metricsConfFile;
        try {
            String selfNodeId = ElasticSearchConfig.getNodeId(pid);
            String selfNodeName = ElasticSearchConfig.getNodeName(pid);
            if(StringUtils.isEmpty(selfNodeId) || StringUtils.isEmpty(selfNodeName)){
                log.error("获取es:{} 的服务信息失败",metricsValueInfo.getJmxConnectionInfo().getName());
            }else{
                HashMap<String,Object> confMap = Yaml.loadType(new FileInputStream(configPath),HashMap.class);
                if(confMap != null){
                    for (String key : confMap.keySet()) {
                        String urlSuffix = key.substring(0,key.lastIndexOf('.'));
                        String url = ElasticSearchConfig.getConnectionUrl(pid) + "/" + urlSuffix;
                        Map<String,String> config = (Map<String, String>) confMap.get(key);

                        String method = config.get("method");
                        String metrics = config.get("metrics");
                        String valuePath = config.get("valuePath").replace("{selfNodeId}",selfNodeId).replace("{selfNodeName}",selfNodeName);
                        String counterType = config.get("counterType");
                        String valueExpress = config.get("valueExpress");

                        String tag = config.get("tag");
                        if("get".equalsIgnoreCase(method)){
                            String responseText = HttpUtil.get(url).getResult();
                            JSONObject jsonObject = JSONObject.parseObject(responseText);
                            if(jsonObject != null){
                                String[] paths = valuePath.split("\\.");
                                for(int i=0;i<paths.length;i++){
                                    if(i == paths.length -1){
                                        Object value = jsonObject.get(paths[i]);
                                        if(value instanceof JSONObject){
                                            log.error("elasticSearch http获取值异常,检查{}路径(valuePath)是否为叶子节点:{}",key,config.get("valuePath"));
                                        }else{
                                            //服务的标识后缀名
                                            String name = metricsValueInfo.getJmxConnectionInfo().getName();

                                            FalconReportObject falconReportObject = new FalconReportObject();
                                            MetricsCommon.setReportCommonValue(falconReportObject,step);
                                            falconReportObject.setTimestamp(metricsValueInfo.getTimestamp());
                                            falconReportObject.setMetric(MetricsCommon.getMetricsName(metrics));

                                            falconReportObject.setValue(String.valueOf(executeJsExpress(valueExpress,value)));

                                            falconReportObject.setCounterType(CounterType.valueOf(counterType));

                                            falconReportObject.appendTags(MetricsCommon.getTags(name,this,serverName(), MetricsType.HTTP_URL_CONF)).
                                                    appendTags(tag);

                                            result.add(falconReportObject);
                                        }
                                    }else{
                                        jsonObject = jsonObject.getJSONObject(paths[i]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("elasticSearch监控值获取发生异常",e);
        }

        return result;
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
        basePropertiesKey = properties.get("basePropertiesKey");
        jmxServerName = properties.get("jmxServerName");
        step = Integer.parseInt(properties.get("step"));
        pluginActivateType = PluginActivateType.valueOf(properties.get("pluginActivateType"));
        pluginDir = properties.get("pluginDir");
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "elasticSearch";
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
     * 能够代表该JMX服务的绝对路径
     * 若实现此方法,则若该JMX连接不可用时,将会检查该JMX服务的目录是否存在,若不存在,将会清除此连接,并不再监控此JMX。
     * 否则,若JMX连接不可用,将会上报不可用的报告,且不会清除
     *
     * @param pid        服务的进程id
     * @param serverName jmx 服务名
     * @return
     */
    @Override
    public String serverPath(int pid, String serverName) {
        String dirPath = "";
        try {
            dirPath = CommandUtilForUnix.getCmdDirByPid(pid);
        } catch (IOException e) {
            log.error("elasticSearch serverDirPath获取异常",e);
        }
        return dirPath;
    }
}
