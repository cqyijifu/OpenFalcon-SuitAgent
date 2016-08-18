/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-10 11:40 创建
 */

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
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/falcon-agent/src/main/resources_ext/conf/log4j.properties");
    }

    /**
     * 统计
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void statistic() throws IOException, InterruptedException {
        DockerMetrics dockerMetrics = new DockerMetrics("192.168.46.22:4232");
        while (true){
            for (DockerMetrics.CollectObject collectObject : dockerMetrics.getMetrics(1000)) {
                System.out.println(collectObject);
            }
            System.out.println();
            Thread.sleep(15000);
        }
    }

    @Test
    public void detect(){
        //ps aux | grep docker
        String msg = "root     13584  0.0  0.1 760820 20156 pts/0    Sl   10:20   0:02 /usr/bin/docker -d --registry-mirror=http://74ecfe5d.m.daocloud.io -H unix:///var/run/docker.sock -H 0.0.0.0:4232\n" +
                "root     13682  0.0  0.0 203160  6584 pts/0    Sl   10:21   0:00 docker-proxy -proto tcp -host-ip 0.0.0.0 -host-port 8080 -container-ip 172.17.0.1 -container-port 8080\n" +
                "root     13815  0.0  0.0 213404  9776 pts/0    Sl   10:22   0:00 docker-proxy -proto tcp -host-ip 0.0.0.0 -host-port 8888 -container-ip 172.17.0.2 -container-port 8080\n" +
                "root     14941  0.0  0.0 103312   876 pts/0    S+   10:59   0:00 grep docker\n" +
                "root     22606  0.0  0.0 227044 14084 ?        Sl   Aug11   4:16 /usr/bin/docker -d\n" +
                "root     23981  0.0  0.0 357988 14900 ?        Sl   Aug11   3:59 /usr/bin/docker -d\n" +
                "root     27750  0.0  0.0 292452 12280 ?        Sl   Aug11   4:19 /usr/bin/docker -d --registry-mirror=http://74ecfe5d.m.daocloud.io\n";
        StringTokenizer st = new StringTokenizer(msg,"\n",false);
        while( st.hasMoreElements() ){
            String split = st.nextToken();
            if(split.contains("-H")){
                String[] ss = split.split("\\s");
                for (String s : ss) {
                    s = s.trim();
                    if(!StringUtils.isEmpty(s)){
                        Matcher matcher = Pattern.compile("^\\d.\\d.\\d.\\d:\\d+$").matcher(s);
                        if(matcher.find()){
                            System.out.println(s);
                        }
                    }
                }
            }
        }

    }

}
