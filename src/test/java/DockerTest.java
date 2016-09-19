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
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        DockerMetrics dockerMetrics = new DockerMetrics("192.168.56.71",8080);
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

}
