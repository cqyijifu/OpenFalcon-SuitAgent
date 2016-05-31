package com.yiji.falcon.agent.jmx;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/3/21.
 */

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.yiji.falcon.agent.jmx.vo.JMXConnectionInfo;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 此类需要具体的监控对象(如ZK,Tomcat等进行继承)
 * Created by QianLong on 16/3/21.
 */
public abstract class JMXConnection {
    private static final Logger log = LoggerFactory.getLogger(JMXConnection.class);
    private static final Map<String,JMXConnectionInfo> connectLibrary = new HashMap<>();//JMX的连接缓存
    private static final Map<String,Integer> serverConnectCount = new HashMap<>();//记录服务应有的JMX连接数
    private static List<JMXConnector> closeRecord = new ArrayList<>();

    /**
     * 根据服务名,返回该服务应有的JMX连接数
     * @param serverName
     * @return
     */
    public static int getServerConnectCount(String serverName){
        return serverConnectCount.get(serverName);
    }

    /**
     * 获取JMX连接
     * @param serverName 要获取的应用的名称(如运行的main类名称)
     * @return
     * null : 应用连接获取失败
     * @throws IOException
     */
    public synchronized List<JMXConnectionInfo> getMBeanConnection(String serverName){
        if(StringUtils.isEmpty(serverName)){
            log.error("获取JMX连接的serverName不能为空");
        }
        //TODO 后期可以加入配置重置刷新JMX连接
        if(this.getClass() == JMXConnection.class){
            log.warn("警告:不应该直接实例化 {} 调用此方法 {}",JMXConnection.class.getName(),"getMBeanConnection()");
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
                        JMXConnector connector = JMXConnectorFactory.connect(url);
                        connections.add(initJMXConnectionInfo(connector,serverName,desc, UUID.randomUUID().toString()));
                        count++;
                    } catch (IOException e) {
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
     * @param serverName
     * 配置中指定的jmx服务名
     * @throws IOException
     */
    public synchronized void resetMBeanConnection(String serverName) {
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
                    JMXConnector connector = JMXConnectorFactory.connect(url);
                    initJMXConnectionInfo(connector,serverName,desc,UUID.randomUUID().toString());
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
     * @param serverName
     * @param desc
     * @param keyId
     * @return
     * @throws IOException
     */
    private JMXConnectionInfo initJMXConnectionInfo(JMXConnector connector,String serverName,VirtualMachineDescriptor desc,String keyId) throws IOException {
        JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
        jmxConnectionInfo.setCacheKeyId(keyId);
        jmxConnectionInfo.setConnectionServerName(serverName);
        jmxConnectionInfo.setConnectionQualifiedServerName(desc.displayName());
        jmxConnectionInfo.setmBeanServerConnection(connector.getMBeanServerConnection());
        jmxConnectionInfo.setName(getJmxConnectionName(connector.getMBeanServerConnection(),Integer.parseInt(desc.id())));
        jmxConnectionInfo.setValid(true);
        jmxConnectionInfo.setPid(Integer.parseInt(desc.id()));

        connectLibrary.put(serverName + keyId,jmxConnectionInfo);
        //添加关闭集合
        closeRecord.add(connector);
        return jmxConnectionInfo;
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
     * 获取指定连接对监控平台暴露标签名
     * @param mBeanServerConnection
     * @return
     */
    public abstract String getJmxConnectionName(MBeanServerConnection mBeanServerConnection,int pid);

}
