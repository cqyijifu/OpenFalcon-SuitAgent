/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.jmx;

import com.yiji.falcon.agent.util.CommandUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class AbstractJmxCommand {

    private final static Logger logger = LoggerFactory.getLogger(AbstractJmxCommand.class);

    private static final String CONNECTOR_ADDRESS =
        "com.sun.management.jmxremote.localConnectorAddress";

    public static String getJVM() {
        return System.getProperty("java.vm.specification.vendor");
    }

    public static boolean isSunJVM() {
        return getJVM().equals("Sun Microsystems Inc.") || getJVM().startsWith("Oracle");
    }

    /**
     * 通过进程id查找JMX的remote连接地址
     * @param pid
     * 查找的进行id
     * @param ip
     * 应用所在的IP地址
     * @return
     * 返回查找的JMX连接地址或查找失败返回Null
     */
    public static String findJMXRemoteUrlByProcessId(int pid,String ip){
        String cmd = "ps aux | grep " + pid;
        String keyStr = "-Dcom.sun.management.jmxremote.port";

        try {
            CommandUtil.ExecuteResult result = CommandUtil.execWithTimeOut(cmd,10, TimeUnit.SECONDS);
            if(result.isSuccess){
                String msg = result.msg;
                StringTokenizer st = new StringTokenizer(msg," ",false);
                while( st.hasMoreElements() ){
                    String split = st.nextToken();
                    if(!StringUtils.isEmpty(split) && split.contains(keyStr)){
                        String[] ss = split.split("=");
                        if(ss.length == 2){
                            return ip + ":" + ss[1];
                        }
                    }
                }
            }else{
                logger.error("命令 {} 执行失败",cmd);
            }
        } catch (Exception e) {
            logger.error("JMX Remote Url 获取异常",e);
            return null;
        }

        return null;
    }

    /**
     * 通过进程id查找JMX的本地连接地址
     *
     * @param pid 查找的进行id
     * @return
     * 返回查找的JMX本地连接地址或查找失败返回Null
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static String findJMXLocalUrlByProcessId(int pid) {

        if (isSunJVM()) {
            try {
                // Classes are all dynamically loaded, since they are specific to Sun VM
                // if it fails for any reason default jmx url will be used

                // tools.jar are not always included used by default class loader, so we
                // will try to use custom loader that will try to load tools.jar

                String javaHome = System.getProperty("java.home");
                String tools = javaHome + File.separator +
                        ".." + File.separator + "lib" + File.separator + "tools.jar";
                URLClassLoader loader = new URLClassLoader(new URL[]{new File(tools).toURI().toURL()});

                Class virtualMachine = Class.forName("com.sun.tools.attach.VirtualMachine", true, loader);
                Class virtualMachineDescriptor = Class.forName("com.sun.tools.attach.VirtualMachineDescriptor", true, loader);

                Method getVMList = virtualMachine.getMethod("list", (Class[])null);
                Method attachToVM = virtualMachine.getMethod("attach", String.class);
                Method getAgentProperties = virtualMachine.getMethod("getAgentProperties", (Class[])null);
                Method getVMId = virtualMachineDescriptor.getMethod("id",  (Class[])null);

                List allVMs = (List)getVMList.invoke(null, (Object[])null);

                for(Object vmInstance : allVMs) {
                    String id = (String)getVMId.invoke(vmInstance, (Object[])null);
                    if (id.equals(Integer.toString(pid))) {

                        Object vm = attachToVM.invoke(null, id);

                        Properties agentProperties = (Properties)getAgentProperties.invoke(vm, (Object[])null);
                        String connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);

                        if (connectorAddress != null) {
                            return connectorAddress;
                        } else {
                            break;
                        }
                    }
                }

                //上面的尝试都不成功，则尝试让agent加载management-agent.jar
                Method getSystemProperties = virtualMachine.getMethod("getSystemProperties", (Class[])null);
                Method loadAgent = virtualMachine.getMethod("loadAgent", String.class, String.class);
                Method detach = virtualMachine.getMethod("detach", (Class[])null);
                for(Object vmInstance : allVMs) {
                    String id = (String)getVMId.invoke(vmInstance, (Object[])null);
                    if (id.equals(Integer.toString(pid))) {

                        Object vm = attachToVM.invoke(null, id);

                        Properties systemProperties = (Properties)getSystemProperties.invoke(vm, (Object[])null);
                        String home = systemProperties.getProperty("java.home");

                        // Normally in ${java.home}/jre/lib/management-agent.jar but might
                        // be in ${java.home}/lib in build environments.

                        String agent = home + File.separator + "jre" + File.separator +
                                           "lib" + File.separator + "management-agent.jar";
                        File f = new File(agent);
                        if (!f.exists()) {
                            agent = home + File.separator +  "lib" + File.separator +
                                        "management-agent.jar";
                            f = new File(agent);
                            if (!f.exists()) {
                                throw new IOException("Management agent not found");
                            }
                        }

                        agent = f.getCanonicalPath();

                        loadAgent.invoke(vm, agent, "com.sun.management.jmxremote");

                        Properties agentProperties = (Properties)getAgentProperties.invoke(vm, (Object[])null);
                        String connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);

                        //detach 这个vm
                        detach.invoke(vm, (Object[])null);

                        if (connectorAddress != null) {
                            return connectorAddress;
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            	return null;
            }
        }

        return null;
    }
}
