/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;

import com.yiji.falcon.agent.jmx.vo.JMXConnectionInfo;
import com.yiji.falcon.agent.jmx.vo.JMXMetricsValueInfo;
import com.yiji.falcon.agent.jmx.vo.JMXObjectNameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.util.*;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class JMXManager {

    private static final Logger log = LoggerFactory.getLogger(JMXManager.class);

    /**
     * 获取指定应用的名称(如运行的main类名称)所有的jmx值
     * @param serverName
     * @return
     * @throws IOException
     * @throws IntrospectionException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     */
    public synchronized static List<JMXMetricsValueInfo> getJmxMetricValue(String serverName, JMXConnection jmxConnection){
        List<JMXConnectionInfo> mbeanConns = jmxConnection.getMBeanConnection(serverName);
        if(mbeanConns == null || mbeanConns.isEmpty()){
            log.error("获取应用 {} jmx连接失败,请检查应用是否已启动",serverName);
            return new ArrayList<>();
        }

        int validCount = 0;
        List<JMXMetricsValueInfo> jmxMetricsValueInfoList = new ArrayList<>();//返回对象
        for (JMXConnectionInfo connectionInfo : mbeanConns) {//遍历JMX连接
            JMXMetricsValueInfo jmxMetricsValueInfo = new JMXMetricsValueInfo();//监控值信息对象
            if(connectionInfo.isValid()){//若该JMX连接可用
                try {
                    List<JMXObjectNameInfo> objectNameList = new ArrayList<>();//该jmx连接下的所有ObjectName值信息
                    Set<ObjectInstance> beanSet = connectionInfo.getmBeanServerConnection().queryMBeans(null, null);
                    for (ObjectInstance mbean : beanSet) {
                        ObjectName objectName = mbean.getObjectName();

                        JMXObjectNameInfo jmxObjectNameInfo = new JMXObjectNameInfo();

                        jmxObjectNameInfo.setObjectName(objectName);
                        jmxObjectNameInfo.setJmxConnectionInfo(connectionInfo);
                        Map<String,String> map = new HashMap<>();
                        for (MBeanAttributeInfo mBeanAttributeInfo : connectionInfo.getmBeanServerConnection().getMBeanInfo(objectName).getAttributes()) {
                            try {
                                String value = connectionInfo.getmBeanServerConnection().getAttribute(mbean.getObjectName(),mBeanAttributeInfo.getName()).toString();
                                map.put(mBeanAttributeInfo.getName(),value);
                            } catch (Exception ignored) {
                            }
                        }

                        jmxObjectNameInfo.setMetricsValue(map);
                        objectNameList.add(jmxObjectNameInfo);
                    }

                    //设置监控值对象
                    jmxMetricsValueInfo.setJmxObjectNameInfoList(objectNameList);

                    validCount++;
                } catch (Exception e) {
                    //jmx 连接取值异常,设置jmx连接为不可用状态,将会在下一次获取连接时进行维护
                    connectionInfo.setValid(false);
                }finally {
                    //设置返回对象-添加监控值对象
                    jmxMetricsValueInfo.setJmxConnectionInfo(connectionInfo);
                    jmxMetricsValueInfoList.add(jmxMetricsValueInfo);
                }
            }else{
                //设置返回对象-添加监控值对象,连接不可用也需要返回,以便于构建连接不可用的报告对象
                jmxMetricsValueInfo.setJmxConnectionInfo(connectionInfo);
                jmxMetricsValueInfoList.add(jmxMetricsValueInfo);
            }
        }

        //若JMX可用的连接数小于该服务应有的JMX连接数,则进行尝试重新构建连接
        //将会在下一次获取监控值时生效
        if(validCount < JMXConnection.getServerConnectCount(serverName)){
            // TODO 这里可以设置重试次数,超过次数就进行此连接的清除
            log.error("发现服务{}有缺失的JMX连接,尝试重新构建该服务的jmx连接",serverName);
            jmxConnection.resetMBeanConnection(serverName);
        }

        return jmxMetricsValueInfoList;
    }

}
