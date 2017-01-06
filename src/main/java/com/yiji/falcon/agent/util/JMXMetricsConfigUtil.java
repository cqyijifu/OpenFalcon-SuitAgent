/*
 * www.yiji.com Inc.
 * Copyright (c) 2017 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2017-01-04 10:55 创建
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.plugins.JMXPlugin;
import com.yiji.falcon.agent.vo.jmx.JMXMetricsConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class JMXMetricsConfigUtil {

    /**
     * 获取需要采集的监控项配置
     * @param jmxPlugin
     * @return
     */
    public static Set<JMXMetricsConfiguration> getMetricsConfig(JMXPlugin jmxPlugin){
        Set<JMXMetricsConfiguration> jmxMetricsConfigurations = new HashSet<>();
        setMetricsConfig("agent.common.metrics.type.", AgentConfiguration.INSTANCE.getJmxCommonMetricsConfPath(), jmxMetricsConfigurations);
        setMetricsConfig(jmxPlugin.basePropertiesKey(),
                AgentConfiguration.INSTANCE.getPluginConfPath() + File.separator + jmxPlugin.configFileName(), jmxMetricsConfigurations);

        return jmxMetricsConfigurations;
    }

    /**
     * 设置配置的jmx监控属性
     *
     * @param basePropertiesKey        配置属性的前缀key值
     * @param propertiesPath           监控属性的配置文件路径
     * @param jmxMetricsConfigurations 需要保存的集合对象
     * @throws IOException
     */
    private static void setMetricsConfig(String basePropertiesKey, String propertiesPath, Set<JMXMetricsConfiguration> jmxMetricsConfigurations) {

        if (!StringUtils.isEmpty(basePropertiesKey) &&
                !StringUtils.isEmpty(propertiesPath)) {
            try (FileInputStream in = new FileInputStream(propertiesPath)) {
                Properties properties = new Properties();
                properties.load(in);
                for (int i = 1; i <= 100; i++) {
                    String objectName = basePropertiesKey + i + ".objectName";
                    if (!StringUtils.isEmpty(properties.getProperty(objectName))) {
                        JMXMetricsConfiguration metricsConfiguration = new JMXMetricsConfiguration();
                        metricsConfiguration.setObjectName(properties.getProperty(objectName));//设置ObjectName
                        metricsConfiguration.setCounterType(properties.getProperty(basePropertiesKey + i + ".counterType"));//设置counterType
                        metricsConfiguration.setMetrics(properties.getProperty(basePropertiesKey + i + ".metrics"));//设置metrics
                        metricsConfiguration.setValueExpress(properties.getProperty(basePropertiesKey + i + ".valueExpress"));//设置metrics
                        String tag = properties.getProperty(basePropertiesKey + i + ".tag");
                        metricsConfiguration.setTag(StringUtils.isEmpty(tag) ? "" : tag);//设置tag
                        String alias = properties.getProperty(basePropertiesKey + i + ".alias");
                        metricsConfiguration.setAlias(StringUtils.isEmpty(alias) ? metricsConfiguration.getMetrics() : alias);

                        jmxMetricsConfigurations.add(metricsConfiguration);
                    }
                }
            } catch (IOException e) {
                log.error("配置文件读取失败", e);
            }
        }
    }
}
