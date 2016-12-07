/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.jmx.vo.JMXConnectionInfo;
import com.yiji.falcon.agent.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class JMXConnection {
    private static final Map<String,JMXConnectionInfo> connectCacheLibrary = new HashMap<>();//JMX的连接缓存
    private static final Map<String,Integer> serverConnectCount = new HashMap<>();//记录服务应有的JMX连接数

    private String serverName;

    public JMXConnection(String serverName) {
        this.serverName = serverName;
    }

    /**
     * 删除JMX连接池连接
     * @param serverName
     * JMX服务名
     * @param pid
     * 进程id
     */
    public static void removeConnectCache(String serverName,int pid){
        String key = serverName + pid;
        if(connectCacheLibrary.remove(key) != null){
            //删除成功,更新serverConnectCount
            int count = serverConnectCount.get(serverName);
            serverConnectCount.put(serverName,count - 1);
            log.info("已清除JMX监控: {} , pid: {}",serverName,pid);
        }
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
                File file = new File(desc.displayName());
                if(file.exists()){
                    //java -jar 形式启动的Java应用
                    if(file.toPath().getFileName().toString().equals(serverName)){
                        return true;
                    }
                }else if(desc.displayName().contains(serverName)){
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
        for (JMXConnectionInfo jmxConnectionInfo : connectCacheLibrary.values()) {
            JMXConnector jmxConnector = jmxConnectionInfo.getJmxConnector();
            if(jmxConnector != null){
                try {
                    jmxConnector.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 获取指定服务名的本地JMX VM 描述对象
     * @param serverName
     * @return
     */
    public static List<VirtualMachineDescriptor> getVmDescByServerName(String serverName){
        List<VirtualMachineDescriptor> vmDescList = new ArrayList<>();
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor desc : vms) {
            File file = new File(desc.displayName());
            if(file.exists()){
                //java -jar 形式启动的Java应用
                if(file.toPath().getFileName().toString().equals(serverName)){
                    vmDescList.add(desc);
                }
            }else if(desc.displayName().contains(serverName)){
                vmDescList.add(desc);
            }
        }
        return vmDescList;
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

        List<VirtualMachineDescriptor> vmDescList = getVmDescByServerName(serverName);

        List<JMXConnectionInfo> connections = connectCacheLibrary.entrySet().
                stream().
                filter(entry -> NumberUtils.isNumber(entry.getKey().replace(serverName,""))).
                map(Map.Entry::getValue).
                collect(Collectors.toList());

        if(connections.isEmpty() || connections.size() < vmDescList.size()){ //JMX连接池为空,进行连接获取
            int count = 0;
            connections.clear();
            clearCache();
            for (VirtualMachineDescriptor desc : vmDescList) {
                JMXConnectUrlInfo jmxConnectUrlInfo = getConnectorAddress(desc);
                if (jmxConnectUrlInfo == null) {
                    log.error("应用 {} 的JMX连接URL获取失败",desc.displayName());
                    //对应的ServerName的JMX连接获取失败，返回该服务JMX连接失败，用于上报不可用记录
                    connections.add(initBadJMXConnect(desc));
                    count++;
                    continue;
                }

                try {
                    connections.add(initJMXConnectionInfo(getJMXConnector(jmxConnectUrlInfo),desc));
                    log.debug("应用 {} JMX 连接已建立",serverName);

                } catch (Exception e) {
                    log.error("JMX 连接获取异常:{}",e.getMessage());
                    //JMX连接获取失败，添加该服务JMX的不可用记录，用于上报不可用记录
                    connections.add(initBadJMXConnect(desc));
                }
                //该服务应有的数量++
                count++;
            }

            if(count > 0){
                serverConnectCount.put(serverName,count);
            }else{
                //对应的ServerName的JMX连接获取失败，返回该服务JMX连接失败，用于上报不可用记录
                JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
                jmxConnectionInfo.setValid(false);
                connections.add(jmxConnectionInfo);
                serverConnectCount.put(serverName,1);
            }
        }

        //若当前应有的服务实例记录值比获取到的记录值小，重新设置
        if(getServerConnectCount(serverName) < vmDescList.size()){
            serverConnectCount.put(serverName,vmDescList.size());
        }

        return connections;
    }

    private JMXConnector getJMXConnector(JMXConnectUrlInfo jmxConnectUrlInfo) throws Exception {
        JMXServiceURL url = new JMXServiceURL(jmxConnectUrlInfo.getRemoteUrl());
        JMXConnector connector;
        if(jmxConnectUrlInfo.isAuthentication()){
            connector = JMXConnectWithTimeout.connectWithTimeout(url,jmxConnectUrlInfo.getJmxUser()
                    ,jmxConnectUrlInfo.getJmxPassword(),10, TimeUnit.SECONDS);
        }else{
            connector = JMXConnectWithTimeout.connectWithTimeout(url,null,null,10, TimeUnit.SECONDS);
        }
        return connector;
    }

    /**
     * 清楚连接缓存
     */
    private void clearCache(){
        //清除当前连接池中的连接
        List<String> removeKey = connectCacheLibrary.keySet().stream().filter(key -> NumberUtils.isNumber(key.replace(serverName,""))).collect(Collectors.toList());
        removeKey.forEach(key -> {
            try {
                JMXConnector jmxConnector = connectCacheLibrary.get(key).getJmxConnector();
                if(jmxConnector != null){
                    jmxConnector.close();
                }

            } catch (IOException ignored) {
            }finally {
                connectCacheLibrary.remove(key);
            }
        });
    }

    /**
     * 重置jmx连接
     * @throws IOException
     */
    public synchronized void resetMBeanConnection() {
        if(StringUtils.isEmpty(serverName)){
            log.error("获取JMX连接的serverName不能为空");
        }

        //本地JMX连接中根据指定的服务名命中的VirtualMachineDescriptor
        List<VirtualMachineDescriptor> targetDesc = getVmDescByServerName(serverName);

        //若命中的target数量大于或等于该服务要求的JMX连接数,则进行重置连接池中的连接
        if(targetDesc.size() >= getServerConnectCount(serverName)){

            //清除当前连接池中的连接
            clearCache();

            //重新设置服务应有连接数
            int count = 0;
            //重新构建连接
            for (VirtualMachineDescriptor desc : targetDesc) {

                JMXConnectUrlInfo jmxConnectUrlInfo = getConnectorAddress(desc);
                if (jmxConnectUrlInfo == null) {
                    log.error("应用{}的JMX连接URL获取失败",serverName);
                    //对应的ServerName的JMX连接获取失败，返回该服务JMX连接失败，用于上报不可用记录
                    initBadJMXConnect(desc);
                    count++;
                    continue;
                }
                try {
                    initJMXConnectionInfo(getJMXConnector(jmxConnectUrlInfo),desc);
                    log.info("应用 {} JMX 连接已建立,将在下一周期获取Metrics值时生效",serverName);
                } catch (Exception e) {
                    log.error("JMX 连接获取异常:{}",e);
                    //JMX连接获取失败，添加该服务JMX的不可用记录，用于上报不可用记录
                    initBadJMXConnect(desc);
                }
                count++;
            }
            serverConnectCount.put(serverName,count);
        }

        //若当前应有的服务实例记录值比获取到的记录值小，重新设置
        if(getServerConnectCount(serverName) < targetDesc.size()){
            serverConnectCount.put(serverName,targetDesc.size());
        }

    }



    private JMXConnectUrlInfo getConnectorAddress(VirtualMachineDescriptor desc){
        if(AgentConfiguration.INSTANCE.isAgentJMXLocalConnect()){
            String connectorAddress = AbstractJmxCommand.findJMXLocalUrlByProcessId(Integer.parseInt(desc.id()));
            if(connectorAddress != null){
                return new JMXConnectUrlInfo(connectorAddress);
            }
        }

        JMXConnectUrlInfo jmxConnectUrlInfo = null;
        try {
            jmxConnectUrlInfo = AbstractJmxCommand.findJMXRemoteUrlByProcessId(Integer.parseInt(desc.id()), InetAddress.getLocalHost().getHostAddress());
            if(jmxConnectUrlInfo != null){
                log.info("JMX Remote URL:{}",jmxConnectUrlInfo);
            }else if(!AgentConfiguration.INSTANCE.isAgentJMXLocalConnect()){
                log.warn("应用未配置JMX Remote功能,请给应用配置JMX Remote");
            }
        } catch (UnknownHostException e) {
            log.error("JMX连接本机地址获取失败",e);
        }
        return jmxConnectUrlInfo;
    }

    /**
     * JMXConnectionInfo的初始化动作
     * @param connector
     * @param desc
     * @return
     * @throws IOException
     */
    private JMXConnectionInfo initJMXConnectionInfo(JMXConnector connector,VirtualMachineDescriptor desc) throws IOException {
        JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
        jmxConnectionInfo.setJmxConnector(connector);
        jmxConnectionInfo.setCacheKeyId(desc.id());
        jmxConnectionInfo.setConnectionServerName(serverName);
        jmxConnectionInfo.setConnectionQualifiedServerName(desc.displayName());
        jmxConnectionInfo.setMBeanServerConnection(connector.getMBeanServerConnection());
        jmxConnectionInfo.setValid(true);
        jmxConnectionInfo.setPid(Integer.parseInt(desc.id()));

        connectCacheLibrary.put(serverName + desc.id(),jmxConnectionInfo);
        return jmxConnectionInfo;
    }

    /**
     * 连接失败的JMX的初始化动作
     * @param desc
     */
    private JMXConnectionInfo initBadJMXConnect(VirtualMachineDescriptor desc){
        JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
        jmxConnectionInfo.setValid(false);
        jmxConnectionInfo.setConnectionServerName(serverName);
        jmxConnectionInfo.setConnectionQualifiedServerName(desc.displayName());
        jmxConnectionInfo.setPid(Integer.parseInt(desc.id()));
        connectCacheLibrary.put(serverName + desc.id(),jmxConnectionInfo);
        return jmxConnectionInfo;
    }

}
