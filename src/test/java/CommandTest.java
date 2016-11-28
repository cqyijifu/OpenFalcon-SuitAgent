/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 10:59 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.plugins.plugin.docker.CAdvisorRunner;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author guqiu@yiji.com
 */
public class CommandTest extends BaseTest{

    @Test
    public void pushDate() throws InterruptedException {
        List<FalconReportObject> list = new ArrayList<>();
        FalconReportObject falconReportObject = new FalconReportObject();
        falconReportObject.setValue("1");
        falconReportObject.setMetric("test");
        falconReportObject.setCounterType(CounterType.COUNTER);
        falconReportObject.setEndpoint("test");
        falconReportObject.setStep(60);
        falconReportObject.setTags("test=test");
        falconReportObject.setTimestamp(System.currentTimeMillis() / 1000);
        for (int i = 0; i <100; i++) {
            list.add(falconReportObject);
        }
        while (true){
            ReportMetrics.push(list);
            Thread.sleep(1000);
        }
    }

    @Test
    public void exec() throws IOException {
//        System.out.println(CommandUtilForUnix.execWithReadTimeLimit("lsof -p 18046",false,7));
//        System.out.println(CommandUtilForUnix.execWithReadTimeLimit("/Users/QianL/Documents/Dev/JavaServer/elasticsearch-2.2.1/bin/elasticsearch",false,5));
//        System.out.println(CommandUtilForUnix.ping("192.168.49.99",5));
    }

    @Test
    public void ping() throws IOException {
        System.out.println(CommandUtilForUnix.ping("www.baidu.com", 5));
    }

    @Test
    public void testJMXRemoteUrl() throws IOException {
        int pid = 0;
        String cmd = "ps aux | grep " + 6346;
        String keyStr = "-Dcom.sun.management.jmxremote.port";

        CommandUtilForUnix.ExecuteResult result = CommandUtilForUnix.execWithReadTimeLimit(cmd, false, 7);
        if (result.isSuccess) {
            String msg = result.msg;
            StringTokenizer st = new StringTokenizer(msg, " ", false);
            while (st.hasMoreElements()) {
                String split = st.nextToken();
                if (!StringUtils.isEmpty(split) && split.contains(keyStr)) {
                    String[] ss = split.split("=");
                    if (ss.length == 2) {
                        System.out.println(ss[1]);
                    }
                }
            }
        } else {
            System.out.println("命令 " + cmd + " 执行失败");
        }

    }

    @Test
    public void jmxAuth() throws IOException, InterruptedException {
        String cmdJavaHome = "echo $JAVA_HOME";
        CommandUtilForUnix.ExecuteResult javaHomeExe = CommandUtilForUnix.execWithReadTimeLimit("/bin/echo", cmdJavaHome, false, 7);
        if (!javaHomeExe.isSuccess) {
            System.out.println("请配置 JAVA_HOME 的系统变量");
            return;
        }
        String javaHome = "/Users/QianL/Desktop/jmx";
        String accessFile = javaHome + "/" + "jre/lib/management/jmxremote.access";
        String passwordFile = javaHome + "/" + "jre/lib/management/jmxremote.password";
        String suffix = ".YijiFalconAgent";
        List<Boolean> results = new ArrayList<>();
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("cp %s %s", accessFile, accessFile + suffix), false, 10).isSuccess);
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("chmod 777 %s", accessFile + suffix), false, 10).isSuccess);
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("cp %s %s", passwordFile, passwordFile + suffix), false, 10).isSuccess);
        results.add(CommandUtilForUnix.execWithReadTimeLimit(String.format("chmod 777 %s", passwordFile + suffix), false, 10).isSuccess);
        if (results.contains(Boolean.FALSE)) {
            System.out.println("JMX的授权文件操作失败");
            CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s", accessFile + suffix), false, 10);
            CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s", passwordFile + suffix), false, 10);
            return;
        }

        String contentForAccess = CommandUtilForUnix.execWithReadTimeLimit(String.format("cat %s", accessFile + suffix), false, 10).msg;
        String user = getJmxUser(contentForAccess);
        System.out.println("jmx user : " + user);
        String contentForPassword = CommandUtilForUnix.execWithReadTimeLimit(String.format("cat %s", passwordFile + suffix), false, 10).msg;
        String password = getJmxPassword(contentForPassword, user);
        System.out.println("jmx password : " + password);


        CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s", accessFile + suffix), false, 10);
        CommandUtilForUnix.execWithReadTimeLimit(String.format("rm -rf %s", passwordFile + suffix), false, 10);

    }

    private String getJmxUser(String content) {
        content = getRidOfCommend(content);
        String[] users = content.split("\n");
        if (users.length < 1) {
            return null;
        }
        String[] user = users[0].split("\\s");
        return user[0].trim();
    }

    private String getJmxPassword(String content, String user) {
        if (user == null) {
            return null;
        }
        content = getRidOfCommend(content);
        String[] passwords = content.split("\n");
        if (passwords.length < 1) {
            return null;
        }

        for (String password : passwords) {
            String[] passwordConf = password.trim().split("\\s");
            if (user.equals(passwordConf[0].trim())) {
                if (passwordConf.length != 2) {
                    return passwordConf[passwordConf.length - 1];
                } else {
                    return passwordConf[1].trim();
                }
            }
        }

        return null;
    }

    private String getRidOfCommend(String content) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(content, "\n", false);
        while (st.hasMoreElements()) {
            String split = st.nextToken().trim();
            if (!StringUtils.isEmpty(split)) {
                if (split.indexOf("#") != 0) {
                    sb.append(split).append("\r\n");
                }
            }
        }
        return sb.toString();
    }

    @Test
    public void cadvisor() throws IOException, InterruptedException {

        CAdvisorRunner cadvisorRunner = new CAdvisorRunner("/home/qianlong/ProjectIDEA/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/plugin/cadvisor", 8089);
        cadvisorRunner.start();

        Thread.sleep(10000);

        cadvisorRunner.shutdownCAdvisor();

        Thread.sleep(5000);
    }

}
