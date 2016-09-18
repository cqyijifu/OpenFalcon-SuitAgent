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
import com.yiji.falcon.agent.util.MD5Util;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author guqiu@yiji.com
 */
public class DockerMetrics {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DockerMetricsUtil dockerRemoteUtil;

    /**
     * 保存第一次监控到的Container的名称
     */
    private static final Set<String> containerNameCache = new HashSet<>();
    /**
     * 容器内运行的命令的解析值
     */
    private static final ConcurrentHashMap<String,String> containerCmdParse = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,String> containerCmd = new ConcurrentHashMap<>();

    /**
     * Docker Metrics 采集
     * @param cadvisorIp
     * @param cadvisorPort
     * @throws IOException
     */
    public DockerMetrics(String cadvisorIp,int cadvisorPort) throws IOException {
        dockerRemoteUtil = new DockerMetricsUtil(cadvisorIp,cadvisorPort);
    }

    /**
     * 数据采集
     * @param interval
     * CPU 采集时间间隔
     * @return
     */
    public List<CollectObject> getMetrics(long interval) throws IOException, InterruptedException {
        List<CollectObject> collectObjectList = new ArrayList<>();
        collectObjectList.add(containerAppMetrics("tomcat","tomcat"));
//        JSONArray containers = dockerRemoteUtil.getContainersJSON();
//        Set<String> containerNames = new HashSet<>();
//        //只保存第一次运行时的容器名称
//        boolean saveContainerName = containerNameCache.isEmpty();
//        for (int i = 0;i<containers.size();i++){
//            JSONObject container = containers.getJSONObject(i);
//            String id = container.getString("Id");
//            JSONArray names = container.getJSONArray("Names");
//            String containerName = getContainerName(names);
//
//            containerNames.add(containerName);
//            if(saveContainerName){
//                containerNameCache.add(containerName);
//            }
//
//            JSONObject result = dockerRemoteUtil.getStatsJSON(id);
//            Thread.sleep(interval);
//            JSONObject result2 = dockerRemoteUtil.getStatsJSON(id);
//
//            collectObjectList.addAll(getCpuMetrics(containerName,result,result2));
//
//            collectObjectList.addAll(getMemMetrics(containerName,result2));
//            collectObjectList.addAll(getNetMetrics(containerName,result2));
//            collectObjectList.add(containerAppMetrics(containerName,id));
//        }
//
//        //容器可用性
//        for (String containerName : containerNameCache) {
//            if(containerNames.contains(containerName)){
//                collectObjectList.add(new CollectObject(containerName,"availability.container","1",""));
//            }else{
//                collectObjectList.add(new CollectObject(containerName,"availability.container","0",""));
//            }
//        }

        return collectObjectList;
    }

    /**
     * 容器内的应用监控
     * @param containerName
     * @param idOrName
     * @return
     * @throws IOException
     */
    private CollectObject containerAppMetrics(String containerName,String idOrName) throws IOException {
        CollectObject collectObject = null;

        String cmd = "/bin/ps aux";
        DockerExecResult execResult = dockerRemoteUtil.exec(cmd,idOrName);
        if(execResult.isSuccess()){
            collectObject = new CollectObject(containerName,"availability.container.app","0","");
            String parseMD5 = psCommandMD5(containerName,execResult.getResult());
            String cache = containerCmdParse.get(containerName);
            if(cache == null){
                //存放第一次的解析值,返回1
                containerCmdParse.put(containerName,parseMD5);
                collectObject.setValue("1");
            }else{
                //比较上一次解析值,若完全匹配,则设置1,代表容器内应用是活动的切与第一次监控的活动情况一致
                if(cache.equals(parseMD5)){
                    collectObject.setValue("1");
                }else{
                    logger.warn("与第一次监控的进程活动不同。\n第一次:{}\n现在的:{}",containerCmd.get(containerName + "-first"),containerCmd.get(containerName));
                }
            }
        }else {
            collectObject = new CollectObject(containerName,"availability.container.app","-1","");
            logger.error("Docker container exec {} execute failed : {}",cmd,execResult.getResult());
        }

        return collectObject;
    }

    /**
     * ps aux 的命令解析
     * @throws IOException
     * @return
     * 返回命令的MD5标识串
     */
    private String psCommandMD5(String containerName,String msg) throws IOException {
        StringTokenizer st = new StringTokenizer(msg,"\n",false);
        List<List<String>> psParseList = new ArrayList<>();
        while( st.hasMoreElements() ){
            String split = st.nextToken().trim();
            if(!StringUtils.isEmpty(split)){
                String[] ss = split.split("\\s+");
                List<String> list = new ArrayList<>();
                Collections.addAll(list, ss);
                psParseList.add(list);
            }
        }

        List<String> titles = psParseList.get(0);
        List<String> command = new ArrayList<>();

        int commandIndex = titles.indexOf("COMMAND");
        for (int i = 1; i < psParseList.size(); i++) {
            List<String> commandContent = psParseList.get(i);
            StringBuilder sb = new StringBuilder();
            for (int j = commandIndex; j < commandContent.size(); j++) {
                //所有的命令
                String str = commandContent.get(j);
                if(!Pattern.matches("\\w*\\d+:\\d+\\w*",str)){
                    //去掉可能出现的时间值,如 11:00 11:00AM
                    sb.append(commandContent.get(j)).append(" ");
                }
            }
            command.add(sb.toString());
        }

        Collections.sort(command,Collections.reverseOrder());
        final StringBuilder sb = new StringBuilder();
        command.forEach(sb::append);

        String result = sb.toString();

        if(containerCmd.get(containerName) == null){
            containerCmd.put(containerName + "-first", result);
        }
        containerCmd.put(containerName, result);


        return MD5Util.getMD5(result);
    }

    private List<CollectObject> getNetMetrics(String containerName,JSONObject result){
        List<CollectObject> collectObjectList = new ArrayList<>();

//        //1.18,1.19,1.20 API版本的网络数据
//        if(dockerVersion.getApiVersion().contains("1.1") || "1.20".equals(dockerVersion.getApiVersion())){
//            JSONObject network = result.getJSONObject("network");
//
//            long rx_bytes = network.getLong("rx_bytes");
//            long rx_packets = network.getLong("rx_packets");
//            long rx_errors = network.getLong("rx_errors");
//            long rx_dropped = network.getLong("rx_dropped");
//
//            long tx_bytes = network.getLong("tx_bytes");
//            long tx_packets = network.getLong("tx_packets");
//            long tx_errors = network.getLong("tx_errors");
//            long tx_dropped = network.getLong("tx_dropped");
//
//            collectObjectList.add(new CollectObject(containerName,"net.if.in.bytes",rx_bytes+"",""));
//            collectObjectList.add(new CollectObject(containerName,"net.if.in.packets",rx_packets+"",""));
//            collectObjectList.add(new CollectObject(containerName,"net.if.in.errors",rx_errors+"",""));
//            collectObjectList.add(new CollectObject(containerName,"net.if.in.dropped",rx_dropped+"",""));
//
//            collectObjectList.add(new CollectObject(containerName,"net.if.out.bytes",tx_bytes+"",""));
//            collectObjectList.add(new CollectObject(containerName,"net.if.out.packets",tx_packets+"",""));
//            collectObjectList.add(new CollectObject(containerName,"net.if.out.errors",tx_errors+"",""));
//            collectObjectList.add(new CollectObject(containerName,"net.if.out.dropped",tx_dropped+"",""));
//        }else{
//            //1.21以及1.21版本以上的网络数据
//            JSONObject networks = result.getJSONObject("networks");
//            for (String ifName : networks.keySet()) {
//                JSONObject ifJson = networks.getJSONObject(ifName);
//
//                String tag = "ifName=" + ifName;
//                long rx_bytes = ifJson.getLong("rx_bytes");
//                long rx_packets = ifJson.getLong("rx_packets");
//                long rx_errors = ifJson.getLong("rx_errors");
//                long rx_dropped = ifJson.getLong("rx_dropped");
//
//                long tx_bytes = ifJson.getLong("tx_bytes");
//                long tx_packets = ifJson.getLong("tx_packets");
//                long tx_errors = ifJson.getLong("tx_errors");
//                long tx_dropped = ifJson.getLong("tx_dropped");
//                collectObjectList.add(new CollectObject(containerName,"net.if.in.bytes",rx_bytes+"",tag));
//                collectObjectList.add(new CollectObject(containerName,"net.if.in.packets",rx_packets+"",tag));
//                collectObjectList.add(new CollectObject(containerName,"net.if.in.errors",rx_errors+"",tag));
//                collectObjectList.add(new CollectObject(containerName,"net.if.in.dropped",rx_dropped+"",tag));
//
//                collectObjectList.add(new CollectObject(containerName,"net.if.out.bytes",tx_bytes+"",tag));
//                collectObjectList.add(new CollectObject(containerName,"net.if.out.packets",tx_packets+"",tag));
//                collectObjectList.add(new CollectObject(containerName,"net.if.out.errors",tx_errors+"",tag));
//                collectObjectList.add(new CollectObject(containerName,"net.if.out.dropped",tx_dropped+"",tag));
//            }
//        }

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
//        JSONObject memory_stats = result.getJSONObject("memory_stats");
//        //热内存使用
////        long total_active_anon = stats.getLong("total_active_anon");
//        //内存已使用原值
//        long usage = memory_stats.getLong("usage");
//        //内存使用最大大小
//        long limit = memory_stats.getLong("limit");
//        //内存使用百分比
//        double rate = Maths.div(usage,limit,5) * 100;
//        collectObjectList.add(new CollectObject(containerName,"mem.total.usage", "" + usage,""));
//        collectObjectList.add(new CollectObject(containerName,"mem.total.limit", "" + limit,""));
//        collectObjectList.add(new CollectObject(containerName,"mem.total.usage.rate", "" + rate,""));
        return collectObjectList;
    }

    /**
     * CPU
     * @param containerName
     * @throws IOException
     * @throws InterruptedException
     */
    private List<CollectObject> getCpuMetrics(String containerName, JSONObject result,JSONObject result2) throws IOException, InterruptedException {
        List<CollectObject> collectObjectList = new ArrayList<>();

//        JSONObject precpu_stats = result.getJSONObject("precpu_stats");
//        JSONObject cpu_usage = precpu_stats.getJSONObject("cpu_usage");
//        long total_usage = cpu_usage.getLong("total_usage");
//        long usage_in_kernelmode = cpu_usage.getLong("usage_in_kernelmode");
//        long usage_in_usermode = cpu_usage.getLong("usage_in_usermode");
//        long system_cpu_usage = precpu_stats.getLong("system_cpu_usage");
//
//        JSONObject precpu_stats2 = result2.getJSONObject("precpu_stats");
//        JSONObject cpu_usage2 = precpu_stats2.getJSONObject("cpu_usage");
//        long total_usage2 = cpu_usage2.getLong("total_usage");
//        long usage_in_kernelmode2 = cpu_usage2.getLong("usage_in_kernelmode");
//        long usage_in_usermode2 = cpu_usage2.getLong("usage_in_usermode");
//        long system_cpu_usage2 = precpu_stats2.getLong("system_cpu_usage");
//
//        //系统占用的CPU总时间
//        long totalSystemUsageCpuTime = system_cpu_usage2 - system_cpu_usage;
//        //Docker CPU总时间
//        long totalDockerUsageCpuTime = total_usage2 - total_usage;
//        //内核CPU总时间
//        long totalKernelCpuTime = usage_in_kernelmode2 - usage_in_kernelmode;
//        //用户CPU总时间
//        long totalUserCpuTime = usage_in_usermode2 - usage_in_usermode;
//        //CPU总时间
//        long totalCpuTime = totalDockerUsageCpuTime + totalSystemUsageCpuTime + totalKernelCpuTime + totalUserCpuTime;
//
//        //总CPU使用率
//        double totalCpuUsageRate = Maths.div(totalDockerUsageCpuTime,totalCpuTime,5) * 100;
//        //内核CPU使用率
//        double kernelUsageRate = Maths.div(totalKernelCpuTime,totalCpuTime,5) * 100;
//        //用户CPU使用率
//        double userUsageRate = Maths.div(totalUserCpuTime,totalCpuTime,5) * 100;
//
//        collectObjectList.add(new CollectObject(containerName,"total.cpu.usage.rate",totalCpuUsageRate + "",""));
//        collectObjectList.add(new CollectObject(containerName,"kernel.cpu.usage.rate",kernelUsageRate + "",""));
//        collectObjectList.add(new CollectObject(containerName,"user.cpu.usage.rate",userUsageRate + "",""));

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
        /**
         * 标签
         */
        private String tags;

        CollectObject(String containerName, String metric, String value,String tags) {
            this.containerName = containerName;
            this.metric = metric;
            this.value = value;
            this.tags = tags;
        }

        @Override
        public String toString() {
            return "CollectObject{" +
                    "containerName='" + containerName + '\'' +
                    ", metric='" + metric + '\'' +
                    ", value='" + value + '\'' +
                    ", tags='" + tags + '\'' +
                    '}';
        }

        public String getTags() {
            return tags == null ? "" : tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
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
