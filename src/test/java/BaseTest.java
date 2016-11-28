/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-11-28 10:46 创建
 */

import org.apache.log4j.PropertyConfigurator;

/**
 * @author guqiu@yiji.com
 */
public class BaseTest {

    static {
        System.setProperty("agent.home.dir", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/target");
        System.setProperty("agent.falcon.dir", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/target");
        System.setProperty("agent.falcon.conf.dir", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/target");
        System.setProperty("agent.plugin.conf.dir", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/target");
        System.setProperty("agent.jmx.metrics.common.path", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/jmx/common.properties");
        System.setProperty("agent.quartz.conf.path", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/quartz.properties");
        System.setProperty("authorization.conf.path", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/authorization.properties");
        System.setProperty("agent.conf.path", "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/agent.properties");
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/log4j.properties");
    }

}
