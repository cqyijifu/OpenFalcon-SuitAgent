/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.ping;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-25 09:58 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.plugins.util.CacheUtil;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.Maths;
import com.yiji.falcon.agent.vo.detect.DetectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guqiu@yiji.com
 */
public class PingPlugin implements DetectPlugin {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int step;
    private final Map<String,String> addresses = new HashMap<>();
    private final ConcurrentHashMap<String,String> tagsCache = new ConcurrentHashMap<>();

    /**
     * 监控的具体服务的agentSignName tag值
     *
     * @param address 被监控的探测地址
     * @return 根据地址提炼的标识, 如域名等
     */
    @Override
    public String agentSignName(String address) {
        String adds = "";
        if(address.contains("[") && address.contains("]")){
            adds = address.substring(0,address.indexOf("["));
        }else{
            adds = address;
        }
        return adds;
    }

    /**
     * 一次地址的探测结果
     *
     * @param address 被探测的地址,地址来源于方法 {@link DetectPlugin#detectAddressCollection()}
     * @return 返回被探测的地址的探测结果, 将用于上报监控状态
     */
    @Override
    public DetectResult detectResult(String address) {
        Map<String,String> map = CacheUtil.getTags(addresses,tagsCache,address);
        String adds = map.get("adds");
        String tags = map.get("tags");

        int pingCount = 5;
        try {
            DetectResult result = new DetectResult();
            result.setCommonTag(tags == null ? "" : tags);
            CommandUtilForUnix.PingResult pingResult = CommandUtilForUnix.ping(adds,pingCount);
            if(pingResult.resultCode == -2){
                //命令执行失败
                result.setSuccess(false);
                //返回探测失败结果
                return result;
            }
            if(pingResult.resultCode == -1){
                result.setSuccess(false);
                //返回探测失败结果
                return result;
            }else{
                //探测成功,并添加延迟值
                result.setSuccess(true);
                List<DetectResult.Metric> metrics = new ArrayList<>();
                metrics.add(new DetectResult.Metric("pingAvgTime",pingResult.avgTime + "", CounterType.GAUGE,null));
                metrics.add(new DetectResult.Metric("pingSuccessRatio",Maths.div(pingResult.successCount,pingCount) + "",CounterType.GAUGE,null));
                result.setMetricsList(metrics);
                return result;
            }
        } catch (IOException e) {
            logger.error("Ping {} 命令执行异常",adds,e);
        }
        return null;
    }

    /**
     * 被探测的地址集合
     *
     * @return 只要该集合不为空, 就会触发监控
     * pluginActivateType属性将不起作用
     */
    @Override
    public Collection<String> detectAddressCollection() {
        CacheUtil.initTagsCache(addresses,tagsCache);
        Set<String> adders = new HashSet<>();
        for (String address : addresses.values()) {
            adders.addAll(helpTransformAddressCollection(address,","));
        }
        return adders;
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
        step = Integer.parseInt(properties.get("step"));
        Set<String> keys = properties.keySet();
        keys.stream().filter(key -> key != null).filter(key -> key.contains("address")).forEach(key -> {
            addresses.put(key,properties.get(key));
        });
    }

    /**
     * 该插件监控的服务名
     * 该服务名会上报到Falcon监控值的tag(service)中,可用于区分监控值服务
     *
     * @return
     */
    @Override
    public String serverName() {
        return "ping";
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
}
