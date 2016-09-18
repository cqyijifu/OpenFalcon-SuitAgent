/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 10:59 创建
 */

import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * @author guqiu@yiji.com
 */
public class CommandTest {

    static {
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/log4j.properties");
    }

    @Test
    public void exec() throws IOException {
        long start = System.currentTimeMillis();
        System.out.println(CommandUtilForUnix.execWithReadTimeLimit(String.format("ping -c %d %s",2,"192.168.1.1"),false,5,TimeUnit.SECONDS));
        System.out.println("执行时间: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void ping() throws IOException {
        System.out.println(CommandUtilForUnix.ping("www.baidu.com",5));
    }

    @Test
    public void testJMXRemoteUrl() throws IOException {
        int pid = 0;
        String cmd = "ps aux | grep " + 6346;
        String keyStr = "-Dcom.sun.management.jmxremote.port";

        CommandUtilForUnix.ExecuteResult result = CommandUtilForUnix.execWithTimeOut(cmd,false,10,TimeUnit.SECONDS);
        if(result.isSuccess){
            String msg = result.msg;
            StringTokenizer st = new StringTokenizer(msg," ",false);
            while( st.hasMoreElements() ){
                String split = st.nextToken();
                if(!StringUtils.isEmpty(split) && split.contains(keyStr)){
                    String[] ss = split.split("=");
                    if(ss.length == 2){
                        System.out.println(ss[1]);
                    }
                }
            }
        }else{
            System.out.println("命令 " + cmd + " 执行失败");
        }

    }

    @Test
    public void jmxAuth() throws IOException, InterruptedException {
        String cmdJavaHome = "echo $JAVA_HOME";
        CommandUtilForUnix.ExecuteResult javaHomeExe = CommandUtilForUnix.execWithTimeOut("/bin/echo",cmdJavaHome,false,10,TimeUnit.SECONDS);
        if(!javaHomeExe.isSuccess){
            System.out.println("请配置 JAVA_HOME 的系统变量");
            return;
        }
        String javaHome = "/Users/QianL/Desktop/jmx";
        String accessFile = javaHome + "/" + "jre/lib/management/jmxremote.access";
        String passwordFile = javaHome + "/" + "jre/lib/management/jmxremote.password";
        String suffix = ".YijiFalconAgent";
        List<Boolean> results = new ArrayList<>();
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("cp %s %s",accessFile,accessFile + suffix),false,10,TimeUnit.SECONDS).isSuccess);
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("chmod 777 %s",accessFile + suffix),false,10,TimeUnit.SECONDS).isSuccess);
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("cp %s %s",passwordFile,passwordFile + suffix),false,10,TimeUnit.SECONDS).isSuccess);
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("chmod 777 %s",passwordFile + suffix),false,10,TimeUnit.SECONDS).isSuccess);
        if(results.contains(Boolean.FALSE)){
            System.out.println("JMX的授权文件操作失败");
            CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s",accessFile + suffix),false,10,TimeUnit.SECONDS);
            CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s",passwordFile + suffix),false,10,TimeUnit.SECONDS);
            return;
        }

        String contentForAccess = CommandUtilForUnix.execWithReadTimeLimit(String.format("cat %s",accessFile + suffix),false,10,TimeUnit.SECONDS).msg;
        String user = getJmxUser(contentForAccess);
        System.out.println("jmx user : " + user);
        String contentForPassword = CommandUtilForUnix.execWithReadTimeLimit(String.format("cat %s",passwordFile + suffix),false,10,TimeUnit.SECONDS).msg;
        String password = getJmxPassword(contentForPassword,user);
        System.out.println("jmx password : " + password);


        CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s",accessFile + suffix),false,10,TimeUnit.SECONDS);
        CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s",passwordFile + suffix),false,10,TimeUnit.SECONDS);

    }

    private String getJmxUser(String content){
        content = getRidOfCommend(content);
        String[] users = content.split("\n");
        if(users.length < 1){
            return null;
        }
        String[] user = users[0].split("\\s");
        return user[0].trim();
    }

    private String getJmxPassword(String content,String user){
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

    private String getRidOfCommend(String content){
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

}
