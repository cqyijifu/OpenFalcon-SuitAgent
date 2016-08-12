/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 命令执行
 * @author guqiu@yiji.com
 */
public class CommandUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommandUtil.class);

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
     * @param timeout
     * @param unit
     * @return
     * @throws IOException
     */
    public static ExecuteResult execWithTimeOut(String cmd, long timeout, TimeUnit unit) throws IOException {
        final ExecuteResult[] executeResult = {new ExecuteResult()};
        final BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(1);
        ExecuteThreadUtil.execute(() -> {
            try {
                executeResult[0] = exec(cmd);
                if (!blockingQueue.offer(executeResult[0]))
                    logger.error("阻塞队列插入失败");
            } catch (Throwable t) {
                blockingQueue.offer(t);
            }
        });

        Object result = BlockingQueueUtil.getResult(blockingQueue,timeout,unit);

        if (result == null)
            throw new IOException("Command \"" + cmd + "\" execute timeout with " + timeout + " " + unit.name());
        if (result instanceof ExecuteResult)
            return (ExecuteResult) result;
        try {
            throw (Throwable) result;
        } catch (IOException | RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.toString(), e);
        }
    }

    /**
     * 执行命令
     * @param cmd
     * 待执行的命令
     * @return
     * @throws IOException
     */
    private static ExecuteResult exec(String cmd) throws IOException {
        ExecuteResult result = new ExecuteResult();

        String[] sh = new String[]{"/bin/sh", "-c", cmd};
        ProcessBuilder pb = new ProcessBuilder(sh);
        Process process = pb.start();

        try(ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
            InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
            InputStream processInStream = new BufferedInputStream(process.getInputStream())){

            int num;
            byte[] bs = new byte[1024];
            while ((num = errorInStream.read(bs)) != -1) {
                resultOutStream.write(bs, 0, num);
            }
            while ((num = processInStream.read(bs)) != -1) {
                resultOutStream.write(bs, 0, num);
                result.isSuccess = true;
            }

            result.msg = new String(resultOutStream.toByteArray(),"utf-8");
        }
        result.isSuccess = process.exitValue() == 0;
        process.destroy();

        return result;
    }

    /**
     * 进行Ping探测
     * @param address
     * 需要Ping的地址
     * @param count
     * Ping的次数
     * @return
     * 返回Ping的平均延时
     * 返回 -1 代表 Ping超时
     * 返回 -2 代表 命令执行失败
     */
    public static PingResult ping(String address,int count) throws IOException {
        PingResult pingResult = new PingResult();
        String commend = String.format("ping -c %d %s",count,address);
        CommandUtil.ExecuteResult executeResult = CommandUtil.execWithTimeOut(commend,10,TimeUnit.SECONDS);

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
                logger.warn(String.format("ping 地址 %s 无法连通",address));
                pingResult.resultCode = -1;
            }else{
                float sum = 0;
                for (Float time : times) {
                    sum += time;
                }
                pingResult.resultCode = 1;
                pingResult.avgTime = Maths.div(sum,times.size(),3);
                pingResult.successCount = times.size();
            }

        }else{
            logger.error("命令{}执行失败",commend);
            pingResult.resultCode = -2;
        }

        return pingResult;
    }

    public static class PingResult{
        /**
         * 执行结果
         * 返回  1 代表执行成功
         * 返回 -1 代表 Ping超时
         * 返回 -2 代表 命令执行失败
         */
        public int resultCode;
        /**
         * 成功返回ping延迟的次数
         */
        public int successCount;
        /**
         * ping的平均延迟值
         */
        public double avgTime;
    }

}