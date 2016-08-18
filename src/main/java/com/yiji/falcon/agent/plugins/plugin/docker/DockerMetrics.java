/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.docker;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-17 17:15 创建
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.util.Maths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guqiu@yiji.com
 */
public class DockerMetrics {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DockerRemoteUtil dockerRemoteUtil;

    /**
     * Docker Metrics 采集
     * @param address
     * Docker daemon 远程地址
     */
    public DockerMetrics(String address) {
        dockerRemoteUtil = new DockerRemoteUtil(address);
    }

    /**
     * 数据采集
     * @param interval
     * CPU 采集时间间隔
     * @return
     */
    public List<CollectObject> getMetrics(long interval) throws IOException, InterruptedException {
        List<CollectObject> collectObjectList = new ArrayList<>();
        JSONArray containers = dockerRemoteUtil.getContainersJSON();
        for (int i = 0;i<containers.size();i++){
            JSONObject container = containers.getJSONObject(i);
            String id = container.getString("Id");
            JSONArray names = container.getJSONArray("Names");
            String containerName = getContainerName(names);

            JSONObject result = dockerRemoteUtil.getStatsJSON(id);
            Thread.sleep(interval);
            JSONObject result2 = dockerRemoteUtil.getStatsJSON(id);

            collectObjectList.addAll(getCpuMetrics(containerName,result,result2));

            collectObjectList.addAll(getMemMetrics(containerName,result2));
            collectObjectList.addAll(getNetMetrics(containerName,result2));
        }
        return collectObjectList;
    }

    private List<CollectObject> getNetMetrics(String containerName,JSONObject result){
        List<CollectObject> collectObjectList = new ArrayList<>();
        JSONObject network = result.getJSONObject("network");

        long rx_bytes = network.getLong("rx_bytes");
        long rx_packets = network.getLong("rx_packets");
        long rx_errors = network.getLong("rx_errors");
        long rx_dropped = network.getLong("rx_dropped");

        long tx_bytes = network.getLong("tx_bytes");
        long tx_packets = network.getLong("tx_packets");
        long tx_errors = network.getLong("tx_errors");
        long tx_dropped = network.getLong("tx_dropped");

        collectObjectList.add(new CollectObject(containerName,"net.if.in.bytes",rx_bytes+""));
        collectObjectList.add(new CollectObject(containerName,"net.if.in.packets",rx_packets+""));
        collectObjectList.add(new CollectObject(containerName,"net.if.in.errors",rx_errors+""));
        collectObjectList.add(new CollectObject(containerName,"net.if.in.dropped",rx_dropped+""));

        collectObjectList.add(new CollectObject(containerName,"net.if.out.bytes",tx_bytes+""));
        collectObjectList.add(new CollectObject(containerName,"net.if.out.packets",tx_packets+""));
        collectObjectList.add(new CollectObject(containerName,"net.if.out.errors",tx_errors+""));
        collectObjectList.add(new CollectObject(containerName,"net.if.out.dropped",tx_dropped+""));

        return collectObjectList;
    }

    /**
     * 内存
     * @param containerName
     * @param result
     * @return
     * @throws IOException
     */
    private List<CollectObject> getMemMetrics(String containerName,JSONObject result) throws IOException {
        List<CollectObject> collectObjectList = new ArrayList<>();
        JSONObject memory_stats = result.getJSONObject("memory_stats");
        JSONObject stats = memory_stats.getJSONObject("stats");
        //热内存使用
//        long total_active_anon = stats.getLong("total_active_anon");
        //内存已使用原值
        long usage = memory_stats.getLong("usage");
        //内存使用最大大小
        long limit = memory_stats.getLong("limit");
        //内存使用百分比
        double rate = Maths.div(usage,limit,5) * 100;
        collectObjectList.add(new CollectObject(containerName,"mem_total_usage", "" + usage));
        collectObjectList.add(new CollectObject(containerName,"mem_total_limit", "" + limit));
        collectObjectList.add(new CollectObject(containerName,"mem_total_usage_rate", "" + rate));
        return collectObjectList;
    }

    /**
     * 获取容器名称
     * @param names
     * @return
     */
    private String getContainerName(JSONArray names){
        String containerName = "";
        for (Object name : names) {
            String name1 = String.valueOf(name);
            if(name1.indexOf("/") == 0){
                name1 = name1.substring(1);
            }
            containerName += "".equals(containerName) ? name1 : ("-" + name1);
        }
        return containerName;
    }

    /**
     * CPU
     * @param containerName
     * @throws IOException
     * @throws InterruptedException
     */
    private List<CollectObject> getCpuMetrics(String containerName, JSONObject result,JSONObject result2) throws IOException, InterruptedException {
        List<CollectObject> collectObjectList = new ArrayList<>();

        JSONObject precpu_stats = result.getJSONObject("precpu_stats");
        JSONObject cpu_usage = precpu_stats.getJSONObject("cpu_usage");
        long total_usage = cpu_usage.getLong("total_usage");
        long usage_in_kernelmode = cpu_usage.getLong("usage_in_kernelmode");
        long usage_in_usermode = cpu_usage.getLong("usage_in_usermode");
        long system_cpu_usage = precpu_stats.getLong("system_cpu_usage");

        JSONObject precpu_stats2 = result2.getJSONObject("precpu_stats");
        JSONObject cpu_usage2 = precpu_stats2.getJSONObject("cpu_usage");
        long total_usage2 = cpu_usage2.getLong("total_usage");
        long usage_in_kernelmode2 = cpu_usage2.getLong("usage_in_kernelmode");
        long usage_in_usermode2 = cpu_usage2.getLong("usage_in_usermode");
        long system_cpu_usage2 = precpu_stats2.getLong("system_cpu_usage");

        //系统占用的CPU总时间
        long totalSystemUsageCpuTime = system_cpu_usage2 - system_cpu_usage;
        //Docker CPU总时间
        long totalDockerUsageCpuTime = total_usage2 - total_usage;
        //内核CPU总时间
        long totalKernelCpuTime = usage_in_kernelmode2 - usage_in_kernelmode;
        //用户CPU总时间
        long totalUserCpuTime = usage_in_usermode2 - usage_in_usermode;
        //CPU总时间
        long totalCpuTime = totalDockerUsageCpuTime + totalSystemUsageCpuTime + totalKernelCpuTime + totalUserCpuTime;

        //总CPU使用率
        double totalCpuUsageRate = Maths.div(totalDockerUsageCpuTime,totalCpuTime,5) * 100;
        //内核CPU使用率
        double kernelUsageRate = Maths.div(totalKernelCpuTime,totalCpuTime,5) * 100;
        //用户CPU使用率
        double userUsageRate = Maths.div(totalUserCpuTime,totalCpuTime,5) * 100;

        collectObjectList.add(new CollectObject(containerName,"total_cpu_usage_rate",totalCpuUsageRate + ""));
        collectObjectList.add(new CollectObject(containerName,"kernel_cpu_usage_rate",kernelUsageRate + ""));
        collectObjectList.add(new CollectObject(containerName,"user_cpu_usage_rate",userUsageRate + ""));

        return collectObjectList;
    }

    public class CollectObject{
        /**
         * 容器名称
         */
        private String containerName;
        /**
         * 指标名称
         */
        private String metric;
        /**
         * 指标值
         */
        private String value;

        CollectObject(String containerName, String metric, String value) {
            this.containerName = containerName;
            this.metric = metric;
            this.value = value;
        }

        @Override
        public String toString() {
            return "CollectObject{" +
                    "containerName='" + containerName + '\'' +
                    ", metric='" + metric + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }
    }
}
