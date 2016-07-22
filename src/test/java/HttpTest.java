/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-22 15:24 创建
 */

import com.yiji.falcon.agent.util.HttpUtil;
import org.junit.Test;

import java.io.IOException;

/**
 * @author guqiu@yiji.com
 */
public class HttpTest {

    @Test
    public void httpsTest() throws IOException {
        System.out.println(HttpUtil.httpsRequest("https://www.baidu.com","GET",null));
    }

    @Test
    public void httpTest() throws IOException {
        System.out.println(HttpUtil.get("http://hahahahahaha123.com"));
    }

}
