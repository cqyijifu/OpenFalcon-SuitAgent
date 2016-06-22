/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.elasticSearch;

import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.MetricsType;
import com.yiji.falcon.agent.jmx.JMXManager;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.plugins.JMXMetricsValue;
import com.yiji.falcon.agent.util.HttpUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class ElasticSearchMetricsValue extends JMXMetricsValue {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取所有的具体服务的JMX监控值VO
     *
     * @return
     */
    @Override
    protected List<JMXMetricsValueInfo> getMetricsValueInfos() {
        return JMXManager.getJmxMetricValue(getServerName(),new ElasticSearchJMXConnection());
    }

    /**
     * 当可用时的內建监控报告
     * 此方法只有在监控对象可用时,才会调用,并加入到所有的监控值报告中(getReportObjects)
     * @param metricsValueInfo
     * 当前的JMXMetricsValueInfo信息
     * @return
     */
    @Override
    protected Collection<FalconReportObject> getInbuiltReportObjectsForValid(JMXMetricsValueInfo metricsValueInfo) {
        // 构建elasticSearch/metricsConf.yml配置中配置的监控值
        int pid = metricsValueInfo.getJmxConnectionInfo().getPid();
        Set<FalconReportObject> result = new HashSet<>();
        String configPath = AgentConfiguration.INSTANCE.getElasticSearchMetricsConfPath();
        try {
            String selfNodeId = ElasticSearchConfig.getNodeId(pid);
            String selfNodeName = ElasticSearchConfig.getNodeName(pid);
            if(StringUtils.isEmpty(selfNodeId) || StringUtils.isEmpty(selfNodeName)){
                logger.error("获取es:{} 的服务信息失败",metricsValueInfo.getJmxConnectionInfo().getName());
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
                            String responseText = HttpUtil.get(url);
                            JSONObject jsonObject = JSONObject.parseObject(responseText);
                            if(jsonObject != null){
                                String[] paths = valuePath.split("\\.");
                                for(int i=0;i<paths.length;i++){
                                    if(i == paths.length -1){
                                        Object value = jsonObject.get(paths[i]);
                                        if(value instanceof JSONObject){
                                            logger.error("elasticSearch http获取值异常,检查{}路径(valuePath)是否为叶子节点:{}",key,config.get("valuePath"));
                                        }else{
                                            //服务的标识后缀名
                                            String name = metricsValueInfo.getJmxConnectionInfo().getName();

                                            FalconReportObject falconReportObject = new FalconReportObject();
                                            setReportCommonValue(falconReportObject);
                                            falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
                                            falconReportObject.setMetric(getMetricsName(metrics));

                                            falconReportObject.setValue(String.valueOf(executeJsExpress(valueExpress,value)));

                                            falconReportObject.setCounterType(CounterType.valueOf(counterType));

                                            falconReportObject.appendTags(getTags(name, MetricsType.HTTPURLCONF)).
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
            logger.error("elasticSearch监控值获取发生异常",e);
        }

        return result;
    }

    /**
     * 获取step
     *
     * @return
     */
    @Override
    public int getStep() {
        return AgentConfiguration.INSTANCE.getElasticSearchStep();
    }

    /**
     * 监控类型
     *
     * @return
     */
    @Override
    public String getType() {
        return "elasticSearch";
    }

    /**
     * 自定义的监控属性的监控值基础配置名
     *
     * @return
     */
    @Override
    public String getBasePropertiesKey() {
        return null;
    }

    /**
     * 自定义的监控属性的配置文件位置
     *
     * @return
     */
    @Override
    public String getMetricsConfPath() {
        return null;
    }

    /**
     * JMX连接的服务名
     *
     * @return
     */
    @Override
    public String getServerName() {
        return AgentConfiguration.INSTANCE.getElasticSearchJmxServerName();
    }
}
