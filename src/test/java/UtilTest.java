/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-09 10:58 创建
 */

import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * @author guqiu@yiji.com
 */
public class UtilTest {

    @Test
    public void test() throws FileNotFoundException {
        System.out.println(NumberUtils.isNumber("234.23423424234242342342"));
    }

}
