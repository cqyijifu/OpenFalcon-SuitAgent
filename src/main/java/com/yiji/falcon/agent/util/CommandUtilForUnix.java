/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.*;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * 命令执行
 * @author guqiu@yiji.com
 */
@Slf4j
public class CommandUtilForUnix {

    /**
     * 命令执行的返回结果值类
     */
    public static class ExecuteResult{
        /**
         * 命令是否执行成功
         */
        public boolean isSuccess = false;
        /**
         * 是否正常执行结束
         * 如结果读取超时、命令执行失败等都是false
         */
        public boolean isNormalExit = false;
        /**
         * 执行命令的返回结果
         */
        public String msg = "";

        public ExecuteResult(boolean isSuccess, boolean isNormalExit, String msg) {
            this.isSuccess = isSuccess;
            this.isNormalExit = isNormalExit;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "ExecuteResult{" +
                    "isSuccess=" + isSuccess +
                    ", isNormalExit=" + isNormalExit +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    /**
     * 获取指定进程的命令行目录
     * @param pid
     * @return
     * @throws IOException
     */
    public static String getCmdDirByPid(int pid) throws IOException {
        String cmd = "ls -al /proc/" + pid;
        CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(cmd,false,7);
        String msg = executeResult.msg;
        String[] ss = msg.split("\n");
        for (String s : ss) {
            if(s.toLowerCase().contains("cwd")){
                String[] split = s.split("\\s+");
                return split[split.length - 1];
            }
        }

        return null;
    }


    /**
     * 执行命令,给定最大的命令结果读取时间(秒)，实际等待读取时间可能会大于指定时间，最大可能为2倍
     * @param execTarget
     * @param cmd
     * @param cmdSep
     * 是否对cmd进行空格分隔
     * @param maxReadTime
     * @return
     * @throws IOException
     */
    public static ExecuteResult execWithReadTimeLimit(String execTarget,String cmd,boolean cmdSep, long maxReadTime){
        final List<Process> resultList = new ArrayList<>();
        final BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(1);
        StringBuilder resultMsg = new StringBuilder();
        ExecuteThreadUtil.execute(() -> {
            Process process = null;
            try {
                process = exec(resultMsg,execTarget,cmd,cmdSep,maxReadTime, TimeUnit.SECONDS);
            } catch (IOException e) {
                log.error("Command '{}' execute exception",cmd,e);
            }
            resultList.add(process);
            if (!blockingQueue.offer(resultList.get(0)))
                log.error("阻塞队列插入失败");
        });

        try {
            Object result = blockingQueue.take();
            if (result instanceof Process){
                //命令正常返回
                Process process = (Process) result;
                if(process.isAlive()){
                    log.debug("process destroyForcibly");
                   process.destroyForcibly();
                }
                boolean exeSuccess = process.exitValue() == 0;
                if(!exeSuccess){
                    log.error("命令 {} 执行错误：{}",cmd,resultMsg.toString());
                }
                return new ExecuteResult(resultMsg.length() > 0 ,exeSuccess,String.valueOf(resultMsg.toString()));
            }else{
                log.error("Unknown Result Type Mapper",result);
                return new ExecuteResult(resultMsg.length() > 0,false,resultMsg.toString());
            }
        } catch (InterruptedException e) {
            log.warn("Command '{}' execute exception",cmd,e);
            return new ExecuteResult(resultMsg.length() > 0,false,resultMsg.toString());
        }

    }

    /**
     * 执行命令,给定最大的命令结果读取时间
     * @param cmd
     * @param cmdSep
     * 是否对cmd进行空格分隔
     * @param maxReadTime
     * @return
     * @throws IOException
     */
    public static ExecuteResult execWithReadTimeLimit(String cmd,boolean cmdSep, long maxReadTime) throws IOException {
        return execWithReadTimeLimit(null,cmd,cmdSep,maxReadTime);
    }

    /**
     * 执行命令
     * @param result
     * 记录返回值
     * @param execTarget
     * 执行体
     * 默认 /bin/sh
     * @param cmd
     * 待执行的命令
     * @param cmdSep
     * 是否对cmd进行空格分隔
     * @return
     * @throws IOException
     */
    private static Process exec(StringBuilder result,String execTarget,String cmd,boolean cmdSep,long timeout,TimeUnit timeUnit) throws IOException {
        List<String> shList = new ArrayList<>();
        if(execTarget == null){
            execTarget = "/bin/sh";
            shList.add(execTarget);
            shList.add("-c");
            log.info("执行命令 : {} -c \"{}\"",execTarget,cmd);
        }else{
            shList.add(execTarget);
            log.info("执行命令 : {} {}",execTarget,cmd);
        }
        if(cmdSep){
            Collections.addAll(shList, cmd.split("\\s"));
        }else{
            shList.add(cmd);
        }

        ProcessBuilder pb = new ProcessBuilder(shList);
        Process process = pb.start();

        try(ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
            InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
            InputStream processInStream = new BufferedInputStream(process.getInputStream())){

            Callable readTask1 = new Callable<String>(){
                @Override
                public String call() throws Exception {
                    readFromStream(result,errorInStream,resultOutStream);
                    return "";
                }
            };

            Callable readTask2 = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    readFromStream(result,processInStream,resultOutStream);
                    return "";
                }
            };

            Future readFuture1 = ExecuteThreadUtil.execute(readTask1);
            Future readFuture2 = ExecuteThreadUtil.execute(readTask2);

            try {
                readFuture1.get(timeout,timeUnit);
                readFuture2.get(timeout,timeUnit);
            } catch (Exception e) {
                log.info("Command '{}' read timeout {} {}",cmd,timeout,timeUnit.name());
            }finally {
                process.destroy();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    log.error("",e);
                }
            }
        }

        return process;
    }

    private static void readFromStream(StringBuilder result,InputStream inputStream,ByteArrayOutputStream resultOutStream) throws IOException {
        int num;
        byte[] bs = new byte[1024];
        while ((num = inputStream.read(bs)) != -1) {
            resultOutStream.write(bs, 0, num);
            result.append(new String(resultOutStream.toByteArray(),"utf-8"));
            resultOutStream.reset();
        }
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
        CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(commend,false,30);

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
                log.warn(String.format("ping 地址 %s 无法连通",address));
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
            log.error("命令{}执行失败",commend);
            pingResult.resultCode = -2;
        }

        return pingResult;
    }

    /**
     * 从 /etc/profile 文件获取JAVA_HOME
     * @return
     * null : 获取失败
     * @throws IOException
     */
    public static String getJavaHomeFromEtcProfile() throws IOException {
        ExecuteResult executeResult = execWithReadTimeLimit("cat /etc/profile",false,7);
        if(!executeResult.isSuccess){
            return null;
        }
        String msg = executeResult.msg;
        StringTokenizer st = new StringTokenizer(msg,"\n",false);
        while( st.hasMoreElements() ){
            String split = st.nextToken().trim();
            if(!StringUtils.isEmpty(split)){
                if(split.contains("JAVA_HOME=")){
                    String[] ss = split.split("=");
                    List<String> list = new ArrayList<>();
                    for (String s : ss) {
                        if(!StringUtils.isEmpty(s)){
                            list.add(s);
                        }
                    }
                    return list.get(list.size() - 1);
                }
            }
        }

        return null;
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

        @Override
        public String toString() {
            return "PingResult{" +
                    "resultCode=" + resultCode +
                    ", successCount=" + successCount +
                    ", avgTime=" + avgTime +
                    '}';
        }
    }

}