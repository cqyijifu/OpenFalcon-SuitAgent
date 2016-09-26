/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 13:42 创建
 */

import com.yiji.falcon.agent.config.AgentConfiguration;

import java.util.concurrent.*;

/**
 * @author guqiu@yiji.com
 */
public class ExecuteThreadUtil {
    private static ExecutorService executorService;
    static {
        final int maxPoolSize = AgentConfiguration.INSTANCE.getAgentMaxThreadCount();
        //定义并发执行服务
        executorService = new ThreadPoolExecutor(5,maxPoolSize,0L,TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread t=new Thread(r);
                    t.setName("agentThreadPool");
                    return t;
                }
        );
    }

    /**
     * 执行线程任务
     * @param task
     */
    public static void execute(Runnable task){
        executorService.submit(task);
    }

    /**
     * 执行线程任务
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Future<T> execute(Callable<T> task){
        return executorService.submit(task);
    }

    /**
     * 关闭线程池
     */
    public static void shutdown(){
        executorService.shutdown();
    }
}
