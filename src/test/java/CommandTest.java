/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 10:59 创建
 */

import com.yiji.falcon.agent.util.CommandUtil;
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
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/falcon-agent/src/main/resources_ext/conf/log4j.properties");
    }

    @Test
    public void exec() throws IOException {
        System.out.println(CommandUtil.getJavaHomeFromEtcProfile());
    }

    @Test
    public void ping() throws IOException {
        int count = 5;
        String address = "www.deh4.com";

        CommandUtil.ExecuteResult executeResult = CommandUtil.execWithTimeOut(String.format("ping -c %d %s",count,address),
                5, TimeUnit.SECONDS);
        if(executeResult.isSuccess){
            List<Float> times = new ArrayList<>();
            String msg = executeResult.msg;
            for (String line : msg.split("\n")) {
                for (String ele : line.split(" ")) {
                    if(ele.toLowerCase().contains("time=")){
                        float time = Float.parseFloat(ele.replace("time=",""));
                        times.add(time);
                    }
                }
            }

            if(times.isEmpty()){
                System.out.println(String.format("ping 地址 %s 无法连通",address));
            }else{
                float sum = 0;
                for (Float time : times) {
                    sum += time;
                }
                System.out.println(String.format("地址 %s 的%s次ping平均延迟 %s",address,count,sum / times.size()));
            }

        }
    }

    @Test
    public void testJMXRemoteUrl() throws IOException {
        int pid = 0;
        String cmd = "ps aux | grep " + 6346;
        String keyStr = "-Dcom.sun.management.jmxremote.port";

        CommandUtil.ExecuteResult result = CommandUtil.execWithTimeOut(cmd,10,TimeUnit.SECONDS);
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
        CommandUtil.ExecuteResult javaHomeExe = CommandUtil.execWithTimeOut("/bin/echo",cmdJavaHome,10,TimeUnit.SECONDS);
        if(!javaHomeExe.isSuccess){
            System.out.println("请配置 JAVA_HOME 的系统变量");
            return;
        }
        String javaHome = "/Users/QianL/Desktop/jmx";
        String accessFile = javaHome + "/" + "jre/lib/management/jmxremote.access";
        String passwordFile = javaHome + "/" + "jre/lib/management/jmxremote.password";
        String suffix = ".YijiFalconAgent";
        List<Boolean> results = new ArrayList<>();
        results.add(CommandUtil.execWithTimeOut(String.format("cp %s %s",accessFile,accessFile + suffix),10,TimeUnit.SECONDS).isSuccess);
        results.add(CommandUtil.execWithTimeOut(String.format("chmod 777 %s",accessFile + suffix),10,TimeUnit.SECONDS).isSuccess);
        results.add(CommandUtil.execWithTimeOut(String.format("cp %s %s",passwordFile,passwordFile + suffix),10,TimeUnit.SECONDS).isSuccess);
        results.add(CommandUtil.execWithTimeOut(String.format("chmod 777 %s",passwordFile + suffix),10,TimeUnit.SECONDS).isSuccess);
        if(results.contains(Boolean.FALSE)){
            System.out.println("JMX的授权文件操作失败");
            CommandUtil.execWithTimeOut(String.format("rm -rf %s",accessFile + suffix),10,TimeUnit.SECONDS);
            CommandUtil.execWithTimeOut(String.format("rm -rf %s",passwordFile + suffix),10,TimeUnit.SECONDS);
            return;
        }

        String contentForAccess = CommandUtil.execWithTimeOut(String.format("cat %s",accessFile + suffix),10,TimeUnit.SECONDS).msg;
        String user = getJmxUser(contentForAccess);
        System.out.println("jmx user : " + user);
        String contentForPassword = CommandUtil.execWithTimeOut(String.format("cat %s",passwordFile + suffix),10,TimeUnit.SECONDS).msg;
        String password = getJmxPassword(contentForPassword,user);
        System.out.println("jmx password : " + password);


        CommandUtil.execWithTimeOut(String.format("rm -rf %s",accessFile + suffix),10,TimeUnit.SECONDS);
        CommandUtil.execWithTimeOut(String.format("rm -rf %s",passwordFile + suffix),10,TimeUnit.SECONDS);

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
