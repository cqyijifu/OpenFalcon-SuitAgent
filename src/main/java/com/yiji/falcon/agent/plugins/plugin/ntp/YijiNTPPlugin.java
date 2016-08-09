/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.ntp;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-08 13:45 创建
 */

import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.util.CommandUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 易极付服务器的NTP监控
 * @author guqiu@yiji.com
 */
public class YijiNTPPlugin implements DetectPlugin {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int step;
    private String address;

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
        String cmd = "ntpdate " + address;
        CommandUtil.ExecuteResult executeResult = null;
        DetectResult detectResult = new DetectResult();
        try {
            executeResult = CommandUtil.exec(cmd);
        } catch (IOException e) {
            logger.error("命令{}执行异常",cmd,e);
        }

        if(executeResult == null || !executeResult.isSuccess){
            logger.error("命令{}执行失败",cmd);
            detectResult.setSuccess(false);
        }else{
            String msg = executeResult.msg;

            int index1 = msg.indexOf("offset");
            int index2 = msg.indexOf("sec");
            detectResult.setSuccess(true);
            double value = Double.parseDouble(msg.substring(index1 + 6, index2).trim());
            Map<String,Double> map = new HashMap<>();
            map.put("ntpOffset",Math.abs(value));
            detectResult.setMetricsMap(map);
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
        Set<String> addresses = new HashSet<>();
        if(!StringUtils.isEmpty(this.address)){
            addresses.add(this.address);
        }
        return addresses;
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
        return "ntp";
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
