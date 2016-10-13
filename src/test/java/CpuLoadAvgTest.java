/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-10-13 16:50 创建
 */

import com.yiji.falcon.agent.util.FileUtil;
import com.yiji.falcon.agent.util.StringUtils;
import org.junit.Test;

/**
 * @author guqiu@yiji.com
 */
public class CpuLoadAvgTest {

    @Test
    public void test(){
        String loadAvg = "0.58 0.76 0.74 1/455 15123";
        if(!StringUtils.isEmpty(loadAvg)){
            String[] ss = loadAvg.split("\\s+");
            System.out.println(ss[0]);
            System.out.println(ss[1]);
            System.out.println(ss[2]);
        }
    }

}
