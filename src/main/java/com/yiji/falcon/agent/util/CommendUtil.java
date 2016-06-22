/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 命令执行
 * @author guqiu@yiji.com
 */
public class CommendUtil {

    /**
     * 命令执行的返回结果值类
     */
    public static class ExecuteResult{
        /**
         * 是否执行成功
         */
        public boolean isSuccess = false;
        /**
         * 执行命令的返回结果
         */
        public String msg = "";

        @Override
        public String toString() {
            return "ExecuteResult{" +
                    "isSuccess=" + isSuccess +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    /**
     * 执行命令
     * @param cmd
     * 待执行的命令
     * @return
     * @throws IOException
     */
    public static ExecuteResult exec(String cmd) throws IOException {
        ExecuteResult result = new ExecuteResult();

        String[] sh = new String[]{"/bin/sh", "-c", cmd};
        ProcessBuilder pb = new ProcessBuilder(sh);
        Process process = pb.start();

        ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
        InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
        InputStream processInStream = new BufferedInputStream(process.getInputStream());
        int num;
        byte[] bs = new byte[1024];
        while ((num = errorInStream.read(bs)) != -1) {
            resultOutStream.write(bs, 0, num);
        }
        while ((num = processInStream.read(bs)) != -1) {
            resultOutStream.write(bs, 0, num);
            result.isSuccess = true;
        }

        result.msg = new String(resultOutStream.toByteArray());

        errorInStream.close();
        processInStream.close();
        resultOutStream.close();
        process.destroy();

        return result;
    }

}