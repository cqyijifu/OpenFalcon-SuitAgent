/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.http;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-21 14:59 创建
 */

import com.yiji.falcon.agent.falcon.CounterType;
import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.plugins.util.CacheUtil;
import com.yiji.falcon.agent.util.HttpUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.HttpResult;
import com.yiji.falcon.agent.vo.detect.DetectResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class HttpPlugin implements DetectPlugin {

    private int step;
    private final Map<String,String> addresses = new HashMap<>();
    private final ConcurrentHashMap<String,String> tagsCache = new ConcurrentHashMap<>();
    private int connectTimeout = 10000;
    private int readTimeout = 10000;

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
        if (properties.get("http.connect.timeout") != null){
            connectTimeout = Integer.parseInt(properties.get("http.connect.timeout"));
        }
        if (properties.get("http.read.timeout") != null){
            readTimeout = Integer.parseInt(properties.get("http.read.timeout"));
        }
        Set<String> keys = properties.keySet();
        keys.stream().filter(Objects::nonNull).filter(key -> key.contains("address")).forEach(key -> {
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
        return "http";
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
        String adder;
        if(address.contains("[") && address.contains("]")){
            adder = address.substring(0,address.indexOf("["));
        }else{
            adder = address;
        }

        AddressParse.Address addObj = AddressParse.parseAddress(adder);

        if(addObj != null && !StringUtils.isEmpty(addObj.url)){
            String url = addObj.url;
            //返回域名
            int index = url.indexOf("/");
            if(index == -1){
                index = url.length();
            }
            return url.substring(0,index);
        }
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
        Map<String,String> map = CacheUtil.getTags(addresses,tagsCache,address);
        String adds = map.get("adds");
        String tags = map.get("tags");

        AddressParse.Address addObj = AddressParse.parseAddress(adds);
        if(addObj != null && !StringUtils.isEmpty(addObj.url)){
            String url = addObj.url;
            DetectResult detectResult = new DetectResult();
            detectResult.setCommonTag(tags);
            if(addObj.isHttp() || addObj.isHttps()){
                boolean isAva = false;
                HttpResult httpResult = null;
                String protocol = addObj.isHttps() ? "https://" : "http://";
                if(addObj.isGetMethod()){
                    try {
                        httpResult = HttpUtil.get(protocol + url,connectTimeout,readTimeout);
                        isAva = true;
                    } catch (Exception e) {
                        detectResult.setSuccess(false);
                    }
                }else if(addObj.isPostMethod()){
                    try {
                        httpResult = HttpUtil.post(null,protocol + url,connectTimeout,readTimeout);
                        isAva = true;
                    } catch (Exception e) {
                        detectResult.setSuccess(false);
                    }
                }else{
                    log.error("请求协议值非法,只能是get或post。您的参数为:{}",adds);
                }

                detectResult.setSuccess(isAva);

                if(isAva && httpResult != null){
                    List<DetectResult.Metric> metricList = new ArrayList<>();

                    metricList.add(new DetectResult.Metric("response.code",String.valueOf(httpResult.getStatus()), CounterType.GAUGE,""));
                    metricList.add(new DetectResult.Metric("response.time",String.valueOf(httpResult.getResponseTime()), CounterType.GAUGE,""));

                    detectResult.setMetricsList(metricList);
                }

            }else{
                log.error("请求协议值非法,只能是http或https。您的参数为:{}",adds);
                return null;
            }

            return detectResult;
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

}
