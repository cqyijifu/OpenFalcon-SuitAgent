/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-10 11:40 创建
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.plugins.plugin.docker.DockerExecResult;
import com.yiji.falcon.agent.plugins.plugin.docker.DockerRemoteUtil;
import com.yiji.falcon.agent.util.JSONUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author guqiu@yiji.com
 */
public class DockerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static {
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/falcon-agent/src/main/resources_ext/conf/log4j.properties");
    }

    @Test
    public void test() throws IOException {
        DockerRemoteUtil dockerRemoteUtil = new DockerRemoteUtil("192.168.46.22:4232");
        JSONArray containers = dockerRemoteUtil.getContainersJSON();
        for (int i = 0;i<containers.size();i++){
            JSONObject container = containers.getJSONObject(i);
            logger.info("容器{}的状态信息:",container.get("Names"));
            String id = container.getString("Id");
            JSONObject result = dockerRemoteUtil.getStatsJSON(id);

            Map<String ,Object> map = JSONUtil.jsonToMap(result,null);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                System.out.println(entry.getKey());
//                System.out.println(entry.getKey() + " : " + entry.getValue() + " : " + entry.getValue().getClass());
            }
        }
    }

    @Test
    public void exec() throws IOException {
        DockerRemoteUtil dockerRemoteUtil = new DockerRemoteUtil("192.168.46.22:4232");
        DockerExecResult execResult = dockerRemoteUtil.exec("/bin/ps -aux","tomcat");
        if(execResult.isSuccess()){
            String msg = execResult.getResult();
            System.out.println(msg);
            System.out.println();
            StringTokenizer st = new StringTokenizer(msg," ",false);
            while( st.hasMoreElements() ){
                String split = st.nextToken();
                if(!StringUtils.isEmpty(split)){
                    System.out.println(split);
                }
            }
        }else {
            System.out.println("命令执行失败");
        }
    }

}
