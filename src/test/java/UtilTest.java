/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-09 10:58 创建
 */

import com.github.kevinsawicki.http.HttpRequest;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * @author guqiu@yiji.com
 */
public class UtilTest {

    @Test
    public void test() throws FileNotFoundException {
        System.out.println(
                HttpRequest.head("http://192.168.46.22:1988/v1/push").connectTimeout(5000).readTimeout(5000).trustAllCerts().trustAllHosts().code()
        );
    }

}
