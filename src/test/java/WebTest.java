/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-26 15:18 创建
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.web.HttpServer;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.IOException;

/**
 * @author guqiu@yiji.com
 */
public class WebTest {

    static {
        PropertyConfigurator.configure("/Users/QianL/Documents/develop/falcon-agent/falcon-agent/src/main/resources/conf/log4j.properties");
    }

    @Test
    public void test() throws IOException {
        HttpServer httpServer = new HttpServer(8080);
        httpServer.startServer();
    }

}
