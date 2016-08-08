/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 10:59 创建
 */

import com.yiji.falcon.agent.util.CommandUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guqiu@yiji.com
 */
public class CommendTest {

    @Test
    public void test() throws IOException {
        int count = 5;
        String address = "192.168.56.254";

        CommandUtil.ExecuteResult executeResult = CommandUtil.exec(String.format("ping -c %d %s",count,address));
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

}
