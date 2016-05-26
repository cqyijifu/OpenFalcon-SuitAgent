package com.yiji.falcon.agent.plugins.elasticSearch;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/23.
 */

import com.yiji.falcon.agent.jmx.JMXConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import java.io.IOException;

/**
 * Created by QianLong on 16/5/23.
 */
public class ElasticSearchJMXConnection extends JMXConnection {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取指定连接对监控平台暴露标签名
     *
     * @param mBeanServerConnection
     * @return
     */
    @Override
    public String getJmxConnectionName(MBeanServerConnection mBeanServerConnection,int pid) {
        try {
            return String.format("%d", ElasticSearchConfig.getHttpPort(pid));
        } catch (IOException e) {
            logger.error("获取elasticSearch的名称异常",e);
            return "";
        }
    }
}
