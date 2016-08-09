/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.yiji.falcon.agent.jmx.vo.JMXConnectionInfo;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 此类需要具体的监控对象(如ZK,Tomcat等进行继承)
 * @author guqiu@yiji.com
 */
public class JMXConnection {
    private static final Logger log = LoggerFactory.getLogger(JMXConnection.class);
    private static final Map<String,JMXConnectionInfo> connectLibrary = new HashMap<>();//JMX的连接缓存
    private static final Map<String,Integer> serverConnectCount = new HashMap<>();//记录服务应有的JMX连接数
    private static List<JMXConnector> closeRecord = new ArrayList<>();

    private String serverName;

    public JMXConnection(String serverName) {
        this.serverName = serverName;
    }

    /**
     * 根据服务名,返回该服务应有的JMX连接数
     * @param serverName
     * @return
     */
    public static int getServerConnectCount(String serverName){
        return serverConnectCount.get(serverName);
    }

    /**
     * 获取本地是否已开启指定的JMX服务
     * @param serverName
     * @return
     */
    public static boolean hasJMXServerInLocal(String serverName){
        if(!StringUtils.isEmpty(serverName)){
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            for (VirtualMachineDescriptor desc : vms) {
                if(desc.displayName().contains(serverName)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @throws IOException
     */
    public static void close() {
        for (JMXConnector jmxConnector : closeRecord) {
            try {
                jmxConnector.close();
            } catch (IOException e) {
                log.warn("",e);
            }
        }
    }

    /**
     * 获取JMX连接
     * @return
     * @throws IOException
     */
    public synchronized List<JMXConnectionInfo> getMBeanConnection(){
        if(StringUtils.isEmpty(serverName)){
            log.error("获取JMX连接的serverName不能为空");
            return new ArrayList<>();
        }
        List<JMXConnectionInfo> connections = connectLibrary.entrySet().
                stream().
                filter(entry -> entry.getKey().contains(serverName)).
                map(Map.Entry::getValue).
                collect(Collectors.toList());
        if(connections.isEmpty()){
            int count = 0;
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            for (VirtualMachineDescriptor desc : vms) {
                if(desc.displayName().contains(serverName)){
                    String connectorAddress = new AbstractJmxCommand().findJMXUrlByProcessId(Integer.parseInt(desc.id()));
                    if (connectorAddress == null) {
                        log.error("应用 {} 的JMX连接URL获取失败",desc.displayName());
                        continue;
                    }

                    try {
                        JMXServiceURL url = new JMXServiceURL(connectorAddress);
                        JMXConnector connector = JMXConnectWithTimeout.connectWithTimeout(url,10, TimeUnit.SECONDS);
                        connections.add(initJMXConnectionInfo(connector,desc, UUID.randomUUID().toString()));
                        count++;
                    } catch (Exception e) {
                        log.error("JMX 连接获取异常",e);
                    }
                }
            }
            if(count > 0){
                serverConnectCount.put(serverName,count);
            }
        }
        return connections;
    }

    /**
     * 重置指定应用的jmx连接
     * 配置中指定的jmx服务名
     * @throws IOException
     */
    public synchronized void resetMBeanConnection() {
        if(StringUtils.isEmpty(serverName)){
            log.error("获取JMX连接的serverName不能为空");
        }
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();

        //本地JMX连接中根据指定的服务名命中的VirtualMachineDescriptor
        List<VirtualMachineDescriptor> targetDesc = new ArrayList<>();

        for (VirtualMachineDescriptor desc : vms) {
            if(desc.displayName().contains(serverName)){
                targetDesc.add(desc);
            }
        }

        //若命中的target数量大于或等于该服务要求的JMX连接数,则进行重置连接池中的连接
        if(targetDesc.size() >= getServerConnectCount(serverName)){
            //清除当前连接池中的连接
            List<String> removeKey = new ArrayList<>();
            for (String key : connectLibrary.keySet()) {
                if(key.contains(serverName)){
                    removeKey.add(key);
                }
            }
            for (String key : removeKey) {
                connectLibrary.remove(key);
            }

            //重新设置服务应有连接数
            int count = 0;
            //重新构建连接
            for (VirtualMachineDescriptor desc : targetDesc) {
                String connectorAddress = new AbstractJmxCommand().findJMXUrlByProcessId(Integer.parseInt(desc.id()));
                if (connectorAddress == null) {
                    log.error("应用{}的JMX连接URL获取失败",serverName);
                    continue;
                }
                try {
                    JMXServiceURL url = new JMXServiceURL(connectorAddress);
                    JMXConnector connector = JMXConnectWithTimeout.connectWithTimeout(url,10, TimeUnit.SECONDS);
                    initJMXConnectionInfo(connector,desc,UUID.randomUUID().toString());
                    count++;
                } catch (IOException e) {
                    log.error("JMX 连接获取异常",e);
                }
            }
            serverConnectCount.put(serverName,count);
        }

    }

    /**
     * JMXConnectionInfo的初始化动作
     * @param connector
     * @param desc
     * @param keyId
     * @return
     * @throws IOException
     */
    private JMXConnectionInfo initJMXConnectionInfo(JMXConnector connector,VirtualMachineDescriptor desc,String keyId) throws IOException {
        JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
        jmxConnectionInfo.setCacheKeyId(keyId);
        jmxConnectionInfo.setConnectionServerName(serverName);
        jmxConnectionInfo.setConnectionQualifiedServerName(desc.displayName());
        jmxConnectionInfo.setmBeanServerConnection(connector.getMBeanServerConnection());
        jmxConnectionInfo.setValid(true);
        jmxConnectionInfo.setPid(Integer.parseInt(desc.id()));

        connectLibrary.put(serverName + keyId,jmxConnectionInfo);
        //添加关闭集合
        closeRecord.add(connector);
        return jmxConnectionInfo;
    }

}
