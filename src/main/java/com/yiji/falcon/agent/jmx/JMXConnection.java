package com.yiji.falcon.agent.jmx;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/3/21.
 */

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.yiji.falcon.agent.jmx.vo.JMXConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 此类需要具体的监控对象(如ZK,Tomcat等进行继承)
 * Created by QianLong on 16/3/21.
 */
public abstract class JMXConnection {
    private static final Logger log = LoggerFactory.getLogger(JMXConnection.class);
    private static final Byte[] LOCK = new Byte[0];
    private static final Map<String,JMXConnectionInfo> connectLibrary = new HashMap<>();

    /**
     * 获取JMX连接
     * @param serverName 要获取的应用的名称(如运行的main类名称)
     * @return
     * null : 应用连接获取失败
     * @throws IOException
     */
    public List<JMXConnectionInfo> getMBeanConnection(String serverName){
        //TODO 后期可以加入配置重置刷新JMX连接
        if(this.getClass() == JMXConnection.class){
            log.warn("警告:不应该直接实例化 {} 调用此方法 {}",JMXConnection.class.getName(),"getMBeanConnection()");
        }
        List<JMXConnectionInfo> connections = connectLibrary.entrySet().
                stream().
                filter(entry -> entry.getKey().contains(serverName)).map(Map.Entry::getValue).collect(Collectors.toList());
        if(connections.size() == 0){
            synchronized (LOCK){
                List<VirtualMachineDescriptor> vms = VirtualMachine.list();
                for (VirtualMachineDescriptor desc : vms) {
                    if(desc.displayName().contains(serverName)){
                        String connectorAddress = new AbstractJmxCommand().findJMXUrlByProcessId(Integer.parseInt(desc.id()));
                        if (connectorAddress == null) {
                            log.error("应用 {} 的JMX连接URL获取失败",desc.displayName());
                            continue;
                        }
                        JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
                        try {
                            JMXServiceURL url = new JMXServiceURL(connectorAddress);
                            JMXConnector connector = JMXConnectorFactory.connect(url);
                            jmxConnectionInfo.setConnectionServerName(serverName);
                            jmxConnectionInfo.setConnectionQualifiedServerName(desc.displayName());
                            jmxConnectionInfo.setmBeanServerConnection(connector.getMBeanServerConnection());
                            jmxConnectionInfo.setName(getJmxConnectionName(connector.getMBeanServerConnection()));
                            jmxConnectionInfo.setValid(true);

                            connections.add(jmxConnectionInfo);
                            connectLibrary.put(desc.displayName(),jmxConnectionInfo);
                        } catch (IOException e) {
                            log.error("JMX 连接获取异常",e);
                        }
                    }
                }
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
    public void resetMBeanConnection(String serverName) {
        synchronized (LOCK){
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            for (VirtualMachineDescriptor desc : vms) {
                if(desc.displayName().contains(serverName)){
                    String connectorAddress = new AbstractJmxCommand().findJMXUrlByProcessId(Integer.parseInt(desc.id()));
                    if (connectorAddress == null) {
//                        log.error("应用{}的JMX连接URL获取失败",serverName);
                        continue;
                    }
                    try {
                        JMXServiceURL url = new JMXServiceURL(connectorAddress);
                        JMXConnector connector = JMXConnectorFactory.connect(url);
                        JMXConnectionInfo jmxConnectionInfo = new JMXConnectionInfo();
                        jmxConnectionInfo.setConnectionServerName(serverName);
                        jmxConnectionInfo.setConnectionQualifiedServerName(desc.displayName());
                        jmxConnectionInfo.setmBeanServerConnection(connector.getMBeanServerConnection());
                        jmxConnectionInfo.setName(getJmxConnectionName(connector.getMBeanServerConnection()));

                        jmxConnectionInfo.setValid(true);
                        connectLibrary.put(desc.displayName(),jmxConnectionInfo);
                    } catch (IOException e) {
                        log.error("JMX 连接获取异常",e);
                    }
                }
            }
        }
    }

    /**
     * 获取指定连接对监控平台暴露标签名
     * @param mBeanServerConnection
     * @return
     */
    public abstract String getJmxConnectionName(MBeanServerConnection mBeanServerConnection);

}
