/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.docker;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-10 11:33 创建
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.HttpUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author guqiu@yiji.com
 */
class DockerMetricsUtil {

    private String urlPrefix;

    /**
     * 创建实例
     * @param cadvisorIp
     * @param cadvisorPort
     */
    DockerMetricsUtil(String cadvisorIp, int cadvisorPort) {
        this.urlPrefix = "http://" + cadvisorIp + ":" + cadvisorPort + "/";
    }

    /**
     * 获取所有的容器
     * @return
     * @throws IOException
     */
    JSONArray getContainersJSON() throws IOException {
        String url = urlPrefix + "api/v1.2/docker/";
        return JSON.parseArray(HttpUtil.get(url,30000,30000).getResult());
    }

    /**
     * 获取指定容器的状态
     * @param containerIdOrName
     * 容器ID或容器名
     * @return
     * @throws IOException
     */
    JSONObject getStatsJSON(String containerIdOrName) throws IOException {
        String url = urlPrefix + "containers/" + containerIdOrName + "/stats?stream=0";
        return JSON.parseObject(HttpUtil.get(url,30000,30000).getResult());
    }

    /**
     * 在指定容器中执行指定命令
     * @param cmd
     * 执行的命令
     * @param containerIdOrName
     * 目标容器id或name
     * @return
     * @throws IOException
     */
    synchronized DockerExecResult exec(String cmd,String containerIdOrName) throws IOException {
        DockerExecResult execResult = new DockerExecResult();

        String dockerExecCmd = String.format("docker exec %s %s",containerIdOrName,cmd);
        CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(dockerExecCmd,false,10, TimeUnit.SECONDS);
        if(!executeResult.isSuccess){
            execResult.setSuccess(false);
        }else{
            String msg = executeResult.msg;
            if(msg.startsWith("rpc error")){
                execResult.setSuccess(false);
                execResult.setResult(msg);
            }else{
                execResult.setSuccess(true);
                execResult.setResult(msg);
            }
        }

        return execResult;
    }

}
