/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.tcp;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-25 13:53 创建
 */

import com.yiji.falcon.agent.plugins.DetectPlugin;
import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.detect.DetectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author guqiu@yiji.com
 */
public class TcpPlugin implements DetectPlugin {

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
        return address;
    }

    /**
     * 一次地址的探测结果
     *
     * @param address 被探测的地址,地址来源于方法 {@link DetectPlugin#detectAddressCollection()}
     * @return 返回被探测的地址的探测结果, 将用于上报监控状态
     */
    @Override
    public DetectResult detectResult(String address) {
        String ipAddr = "";
        int port = 80;
        String[] ss = address.split(":");
        if(ss.length == 1){
            ipAddr = ss[0];
        }else if(ss.length == 2){
            ipAddr = ss[0];
            port = Integer.parseInt(ss[1]);
        }else{
            logger.error("地址配置:{} 非法,请确定是否符合 address:port格式",address);
            return null;
        }
        DetectResult detectResult = new DetectResult();
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName(ipAddr),port),5000);
            if(socket.isConnected()){
                detectResult.setSuccess(true);
            }else{
                logger.warn("tcp地址:{} 连接失败");
                detectResult.setSuccess(false);
            }
        } catch (IOException e) {
            detectResult.setSuccess(false);
        }finally {
            if(socket != null && socket.isConnected()){
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
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
            for (String address : this.address.split(",")) {
                address = address.trim();
                if(!StringUtils.isEmpty(address)){
                    addresses.add(address);
                }
            }
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
        return "tcp";
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
