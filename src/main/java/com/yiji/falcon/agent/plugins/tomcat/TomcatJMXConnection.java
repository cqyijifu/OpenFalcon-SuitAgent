package com.yiji.falcon.agent.plugins.tomcat;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/3.
 */

import com.yiji.falcon.agent.jmx.JMXConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.Set;

/**
 * Created by QianLong on 16/5/3.
 */
public class TomcatJMXConnection extends JMXConnection {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 获取指定连接对监控平台暴露标签名
     *
     * @param mBeanServerConnection
     * @return
     */
    @Override
    public String getJmxConnectionName(MBeanServerConnection mBeanServerConnection) {
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
}
