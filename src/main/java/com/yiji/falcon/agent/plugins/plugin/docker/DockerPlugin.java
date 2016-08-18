/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.docker;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-10 11:15 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.util.CommandUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Docker的监控插件
 * @author guqiu@yiji.com
 */
public class DockerPlugin implements DetectPlugin {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int step;
    private String address;

    /**
     * 自动探测地址的实现
     * 若配置文件已配置地址,将不会调用此方法
     * 若配置文件未配置探测地址的情况下,将会调用此方法,若该方法返回非null且有元素的集合,则启动运行插件,使用该方法返回的探测地址进行监控
     *
     * @return
     */
    @Override
    public Collection<String> autoDetectAddress() {
        List<String> addresses = new ArrayList<>();
        try {
            CommandUtil.ExecuteResult executeResult = CommandUtil.execWithTimeOut("ps aux | grep docker",10, TimeUnit.SECONDS);
            if(!executeResult.isSuccess){
                return null;
            }
            String msg = executeResult.msg;
            StringTokenizer st = new StringTokenizer(msg,"\n",false);
            while( st.hasMoreElements() ){
                String split = st.nextToken();
                if(split.contains("-H")){
                    String[] ss = split.split("\\s");
                    for (String s : ss) {
                        s = s.trim();
                        if(!StringUtils.isEmpty(s)){
                            Matcher matcher = Pattern.compile("^\\d.\\d.\\d.\\d:\\d+$").matcher(s);
                            if(matcher.find()){
                                addresses.add(s);
                                logger.info("Docker 自动探测连接地址: {}",s);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("自动地址探测异常");
            return null;
        }
        return addresses;
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
        DockerMetrics dockerMetrics = new DockerMetrics(address);
        try {
            List<DockerMetrics.CollectObject> collectObjectList = dockerMetrics.getMetrics(1000);

            List<DetectResult.Metric> metrics = new ArrayList<>();
            for (DockerMetrics.CollectObject collectObject : collectObjectList) {
                DetectResult.Metric metric = new DetectResult.Metric(collectObject.getMetric(),
                        collectObject.getValue(),
                        CounterType.GAUGE,
                        "containerName=" + collectObject.getContainerName());
                metrics.add(metric);
            }
            detectResult.setMetricsList(metrics);

            detectResult.setSuccess(true);
        } catch (Exception e) {
            logger.error("Docker数据采集异常",e);
            detectResult.setSuccess(false);
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
        return helpTransformAddressCollection(this.address,",");
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
        this.address = properties.get("address");
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "docker";
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
}
