/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.cpuLoadavg;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-10-13 16:30 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.util.FileUtil;
import com.yiji.falcon.agent.util.Maths;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;

import java.io.File;
import java.util.*;

/**
 * CPU的平均负载/CPU核数
 * @author guqiu@yiji.com
 */
public class CpuLoadAvgByCpuCoreNum implements DetectPlugin{

    private int step;

    @Override
    public String configFileName() {
        return "cpuLoadAvgByCpuCoreNumPlugin.properties";
    }

    @Override
    public Collection<String> autoDetectAddress() {
        File file = new File("/proc/loadavg");
        if(file.exists()){
            //若存在/proc/loadavg文件，返回文件地址，使插件启动
            return Collections.singletonList("/proc/loadavg");
        }
        return new ArrayList<>();
    }

    /**
     * 插件初始化操作
     * 该方法将会在插件运行前进行调用
     *
     * @param properties 包含的配置:
     *                   1、插件目录绝对路径的(key 为 pluginDir),可利用此属性进行插件自定制资源文件读取
     *                   2、插件指定的配置文件的全部配置信息(参见 {@link Plugin#configFileName()} 接口项)
     *                   3、授权配置项(参见 {@link Plugin#authorizationKeyPrefix()} 接口项
     */
    @Override
    public void init(Map<String, String> properties) {
        this.step = Integer.parseInt(properties.get("step"));
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "cpuLoadAvgByCpuCoreNum";
    }

    /**
     * 监控值的获取和上报周期(秒)
     *
     * @return
     */
    @Override
    public int step() {
        return this.step;
    }

    /**
     * Agent关闭时的调用钩子
     * 如，可用于插件的资源释放等操作
     */
    @Override
    public void agentShutdownHook() {

    }

    /**
     * 监控的具体服务的agentSignName tag值
     *
     * @param address 被监控的探测地址
     * @return 根据地址提炼的标识, 如域名等
     */
    @Override
    public String agentSignName(String address) {
        return null;
    }

    /**
     * 一次地址的探测结果
     *
     * @param address 被探测的地址,地址来源于方法 {@link DetectPlugin#detectAddressCollection()}
     * @return 返回被探测的地址的探测结果, 将用于上报监控状态
     */
    @Override
    public DetectResult detectResult(String address) {
        DetectResult detectResult = new DetectResult();
        String loadAvg = FileUtil.getTextFileContent(address);
        if(!StringUtils.isEmpty(loadAvg)){
            String[] ss = loadAvg.split("\\s+");
            double avg1Min = Double.parseDouble(ss[0]);
            double avg5Min = Double.parseDouble(ss[1]);
            double avg15Min = Double.parseDouble(ss[2]);

            int cpuNum = Runtime.getRuntime().availableProcessors();

            avg1Min = Maths.div(avg1Min,cpuNum,3);
            avg5Min = Maths.div(avg5Min,cpuNum,3);
            avg15Min = Maths.div(avg15Min,cpuNum,3);

            List<DetectResult.Metric> metricList = new ArrayList<>();
            Collections.addAll(metricList,
                    new DetectResult.Metric("cpuCoreNum.loadAvg.1",String.valueOf(avg1Min), CounterType.GAUGE,""),
                    new DetectResult.Metric("cpuCoreNum.loadAvg.5",String.valueOf(avg5Min), CounterType.GAUGE,""),
                    new DetectResult.Metric("cpuCoreNum.loadAvg.15",String.valueOf(avg15Min), CounterType.GAUGE,"")
            );
            detectResult.setMetricsList(metricList);
            detectResult.setSuccess(true);
        }
        return detectResult;
    }

    /**
     * 被探测的地址集合
     *
     * @return 只要该集合不为空, 就会触发监控
     * pluginActivateType属性将不起作用
     */
    @Override
    public Collection<String> detectAddressCollection() {
        return new ArrayList<>();
    }
}
