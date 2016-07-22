/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.job;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-15 13:46 创建
 */

import com.yiji.falcon.agent.falcon.ReportMetrics;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;

/**
 * @author guqiu@yiji.com
 */
public class JobThread extends Thread {

    private MetricsCommon metricsValue;

    public JobThread(MetricsCommon metricsValue, String threadName) {
        this.metricsValue = metricsValue;
        this.setName(threadName);
    }

    public JobThread(MetricsCommon metricsValue) {
        this.metricsValue = metricsValue;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        ReportMetrics.push(metricsValue.getReportObjects());
    }
}
