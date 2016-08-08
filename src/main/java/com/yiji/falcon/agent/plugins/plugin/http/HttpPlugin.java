/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.http;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-21 14:59 创建
 */

import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.util.HttpUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.HttpResult;
import com.yiji.falcon.agent.vo.detect.DetectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author guqiu@yiji.com
 */
public class HttpPlugin implements DetectPlugin {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int step;
    private String address;

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
        address = properties.get("address");
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
     * 监控的具体服务的agentSignName tag值
     *
     * @param address 被监控的探测地址
     * @return 根据地址提炼的标识, 如域名等
     */
    @Override
    public String agentSignName(String address) {
        AddressParse.Address addObj = AddressParse.parseAddress(address);

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
        AddressParse.Address addObj = AddressParse.parseAddress(address);
        if(addObj != null && !StringUtils.isEmpty(addObj.url)){
            String url = addObj.url;
            DetectResult detectResult = new DetectResult();
            if(addObj.isHttp() || addObj.isHttps()){
                String protocol = addObj.isHttps() ? "https://" : "http://";
                if(addObj.isGetMethod()){
                    try {
                        HttpResult httpResult = HttpUtil.get(protocol + url);
                        if(httpResult.getStatus() >= 400){
                            detectResult.setSuccess(false);
                        }else{
                            detectResult.setSuccess(true);
                        }
                    } catch (Exception e) {
                        detectResult.setSuccess(false);
                    }
                }else if(addObj.isPostMethod()){
                    try {
                        HttpResult httpResult = HttpUtil.post(null,protocol + url);
                        if(httpResult != null && httpResult.getStatus() >= 400){
                            detectResult.setSuccess(false);
                        }else{
                            detectResult.setSuccess(true);
                        }
                    } catch (Exception e) {
                        detectResult.setSuccess(false);
                    }
                }else{
                    logger.error("请求协议值非法,只能是get或post。您的参数为:{}",address);
                }
            }else{
                logger.error("请求协议值非法,只能是http或https。您的参数为:{}",address);
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
        Set<String> addressSet = new HashSet<>();
        if(!StringUtils.isEmpty(address)){
            Collections.addAll(addressSet, address.split(","));
        }
        return addressSet;
    }

}
