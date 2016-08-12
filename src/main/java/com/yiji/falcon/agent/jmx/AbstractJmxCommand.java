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
import java.util.ArrayList;
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
     * 返回查找的JMX连接地址对象或查找失败返回Null
     */
    public static JMXConnectUrlInfo findJMXRemoteUrlByProcessId(int pid, String ip){
        String cmd = "ps aux | grep " + pid;
        logger.info("JMX Remote Target Pid:{}", pid);
        String jmxPortOpt = "-Dcom.sun.management.jmxremote.port";
        String authPortOpt = "-Dcom.sun.management.jmxremote.authenticate";

        try {
            JMXConnectUrlInfo remoteUrlInfo = new JMXConnectUrlInfo();

            CommandUtil.ExecuteResult result = CommandUtil.execWithTimeOut(cmd,10, TimeUnit.SECONDS);

            if(result.isSuccess){
                String msg = result.msg;
                StringTokenizer st = new StringTokenizer(msg," ",false);
                while( st.hasMoreElements() ){
                    String split = st.nextToken();
                    if(!StringUtils.isEmpty(split)){
                        if(split.contains(jmxPortOpt)){
                            String[] ss = split.split("=");
                            if(ss.length == 2){
                                remoteUrlInfo.setRemoteUrl("service:jmx:rmi:///jndi/rmi://" + ip + ":" + ss[1].trim() + "/jmxrmi");
                            }
                        }else if(split.contains(authPortOpt)){
                            //是否需要JMX Remote 认证
                            String[] ss = split.split("=");
                            boolean isAuth = "true".equalsIgnoreCase(ss[1].trim());
                            remoteUrlInfo.setAuthentication(isAuth);
                            if(isAuth){
                                //寻找JMX的认证信息
                                String cmdJavaHome = "echo $JAVA_HOME";
                                CommandUtil.ExecuteResult javaHomeExe = CommandUtil.execWithTimeOut(cmdJavaHome,10,TimeUnit.SECONDS);
                                if(!javaHomeExe.isSuccess){
                                    logger.error("请配置 JAVA_HOME 的系统变量");
                                    return null;
                                }
                                String javaHome = javaHomeExe.msg;
                                String accessFile = javaHome + "/" + "jre/lib/management/jmxremote.access";
                                String passwordFile = javaHome + "/" + "jre/lib/management/jmxremote.password";
                                String suffix = ".YijiFalconAgent";
                                List<Boolean> results = new ArrayList<>();
                                //因java授权文件有严格的读取权限控制,为能够读到授权文件信息,创建临时文件
                                results.add(CommandUtil.execWithTimeOut(String.format("cp %s %s",accessFile,accessFile + suffix),10,TimeUnit.SECONDS).isSuccess);
                                results.add(CommandUtil.execWithTimeOut(String.format("chmod 777 %s",accessFile + suffix),10,TimeUnit.SECONDS).isSuccess);
                                results.add(CommandUtil.execWithTimeOut(String.format("cp %s %s",passwordFile,passwordFile + suffix),10,TimeUnit.SECONDS).isSuccess);
                                results.add(CommandUtil.execWithTimeOut(String.format("chmod 777 %s",passwordFile + suffix),10,TimeUnit.SECONDS).isSuccess);
                                if(results.contains(Boolean.FALSE)){
                                    logger.error("JMX的授权文件操作失败");
                                    //删除临时文件
                                    CommandUtil.execWithTimeOut(String.format("rm -rf %s",accessFile + suffix),10,TimeUnit.SECONDS);
                                    CommandUtil.execWithTimeOut(String.format("rm -rf %s",passwordFile + suffix),10,TimeUnit.SECONDS);
                                    return null;
                                }

                                String contentForAccess = CommandUtil.execWithTimeOut(String.format("cat %s",accessFile + suffix),10,TimeUnit.SECONDS).msg;
                                String user = getJmxUser(contentForAccess);
                                String contentForPassword = CommandUtil.execWithTimeOut(String.format("cat %s",passwordFile + suffix),10,TimeUnit.SECONDS).msg;
                                String password = getJmxPassword(contentForPassword,user);

                                //删除临时文件
                                CommandUtil.execWithTimeOut(String.format("rm -rf %s",accessFile + suffix),10,TimeUnit.SECONDS);
                                CommandUtil.execWithTimeOut(String.format("rm -rf %s",passwordFile + suffix),10,TimeUnit.SECONDS);

                                remoteUrlInfo.setJmxUser(user);
                                remoteUrlInfo.setJmxPassword(password);

                                if(StringUtils.isEmpty(user) || StringUtils.isEmpty(password)){
                                    logger.error("JMX Remote 的认证User 和Password 获取失败");
                                }
                            }
                        }
                    }
                }
            }else{
                logger.error("命令 {} 执行失败",cmd);
                return null;
            }

            return remoteUrlInfo;
        } catch (Exception e) {
            logger.error("JMX Remote Url 获取异常",e);
            return null;
        }

    }

    /**
     * 获取JMX授权用户
     * @param content
     * @return
     */
    private static String getJmxUser(String content){
        content = getRidOfCommend(content);
        String[] users = content.split("\n");
        if(users.length < 1){
            return null;
        }
        String[] user = users[0].split("\\s");
        return user[0].trim();
    }

    /**
     * 获取JMX授权密码
     * @param content
     * @param user
     * @return
     */
    private static String getJmxPassword(String content,String user){
        if(user == null){
            return null;
        }
        content = getRidOfCommend(content);
        String[] passwords = content.split("\n");
        if(passwords.length < 1){
            return null;
        }

        for (String password : passwords) {
            String[] passwordConf = password.trim().split("\\s");
            if(user.equals(passwordConf[0].trim())){
                if(passwordConf.length != 2){
                    return passwordConf[passwordConf.length - 1];
                }else{
                    return passwordConf[1].trim();
                }
            }
        }

        return null;
    }

    /**
     * 去掉注释行
     * @param content
     * @return
     */
    private static String getRidOfCommend(String content){
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(content,"\n",false);
        while( st.hasMoreElements() ){
            String split = st.nextToken().trim();
            if(!StringUtils.isEmpty(split)){
                if(split.indexOf("#") != 0){
                    sb.append(split).append("\r\n");
                }
            }
        }
        return sb.toString();
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
