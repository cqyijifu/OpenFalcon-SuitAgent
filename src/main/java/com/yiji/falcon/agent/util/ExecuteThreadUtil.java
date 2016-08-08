/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 13:42 创建
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author guqiu@yiji.com
 */
public class ExecuteThreadUtil {
    private static ExecutorService executorService;
    static {
        final int cpuCore = Runtime.getRuntime().availableProcessors();
        //线程数
        final int poolSize = cpuCore * 3;
        //定义并发执行服务
        executorService = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t=new Thread(r);
            t.setName("agentThreadPool");
            return t;
        });
    }

    /**
     * 执行一个无返回值的线程任务
     * 注：若执行的线程任务是长时间运行的线程，
     * 请不要用此线程池进行线程任务的创建，会有死锁的隐患
     * @param task
     */
    public static void execute(Runnable task){
        executorService.submit(task);
    }
}
