/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-10 11:40 创建
 */

import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.plugins.plugin.docker.DockerMetrics;
import com.yiji.falcon.agent.util.DateUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guqiu@yiji.com
 */
public class DockerTest{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static {
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/src/main/resources_ext/conf/log4j.properties");
    }

    /**
     * 统计
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void statistic() throws IOException, InterruptedException {
        DockerMetrics dockerMetrics = new DockerMetrics("192.168.46.22",8080);
        dockerMetrics.getMetrics().forEach(System.out::println);
    }

    @Test
    public void detect(){
        String msg = "CONTAINER ID        IMAGE                    COMMAND                  CREATED             STATUS              PORTS                    NAMES\n" +
                "b28781d38b43        google/cadvisor:latest   \"/usr/bin/cadvisor -l\"   4 minutes ago       Up 4 minutes        0.0.0.0:8080->8080/tcp   cadvisor\n" +
                "d5e906d898dd        tomcat                   \"sh /opt/apache-tomca\"   About an hour ago   Up About an hour                             tomcat-mem\n" +
                "5589e40c6da7        tomcat                   \"sh /opt/apache-tomca\"   5 days ago          Up 18 hours                                  tomcat2\n" +
                "f326917da17e        tomcat                   \"sh /opt/apache-tomca\"   6 days ago          Up 18 hours                                  tomcat\n";
        StringTokenizer st = new StringTokenizer(msg,"\n",false);
        while( st.hasMoreElements() ){
            String split = st.nextToken();
            if(split.contains("google/cadvisor")){
                String[] ss = split.split("\\s+");
                for (String s : ss) {
                    if(s.contains("->")){
                        String[] ss2 = s.trim().split("->");
                        for (String s1 : ss2[0].split(":")) {
                            if(s1.matches("\\d+")){
                                System.out.println(s1);
                            }
                        }
                    }
                }
            }
        }

    }

    @Test
    public void time(){
        String timestamp ="2016-09-20T03:12:33.10444672Z";
        if(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6,}Z")){
            String picoseconds = timestamp.substring(20, timestamp.length() - 1);
            System.out.println(picoseconds);
            String dateTime = timestamp.substring(0,10) + " " + timestamp.substring(11,19);

            Date date = DateUtil.getParrtenDate(dateTime,"yyyy-MM-dd HH:mm:ss");

            assert date != null;
            long microseconds = date.getTime();
            System.out.println("原Date转换纳秒 " + microseconds * 1000);

            String trans = String.valueOf(microseconds / 1000) + picoseconds.substring(0,6);

            System.out.println("转换后的纳秒   " + trans);
        }
    }

}
