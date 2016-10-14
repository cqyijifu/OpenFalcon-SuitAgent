/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-09 10:58 创建
 */

import org.ho.yaml.Yaml;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guqiu@yiji.com
 */
public class UtilTest {

    @Test
    public void test() throws FileNotFoundException {
        Map<String,Map<String,Object>> updateConf = Yaml.loadType(new FileInputStream("/Users/QianL/Documents/develop/falcon-agent/SuitAgent-Update/3.0/updateList.yml"),HashMap.class);
        System.out.println(updateConf);
    }

}
