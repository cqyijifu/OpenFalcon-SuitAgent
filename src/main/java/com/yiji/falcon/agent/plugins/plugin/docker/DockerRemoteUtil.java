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
import com.yiji.falcon.agent.util.HttpUtil;
import com.yiji.falcon.agent.util.StringUtils;

import java.io.IOException;

/**
 * @author guqiu@yiji.com
 */
public class DockerRemoteUtil {

    private String urlPrefix;

    /**
     * 创建实例
     * @param remoteAddress
     * Docker Remote API 的连接地址
     */
    public DockerRemoteUtil(String remoteAddress) {
        this.urlPrefix = "http://" + remoteAddress + "/";
    }

    /**
     * 获取所有的容器
     * @return
     * @throws IOException
     */
    public JSONArray getContainersJSON() throws IOException {
        String url = urlPrefix + "containers/json";
        return JSON.parseArray(HttpUtil.get(url).getResult());
    }

    /**
     * 获取指定容器的状态
     * @param containerIdOrName
     * 容器ID或容器名
     * @return
     * @throws IOException
     */
    public JSONObject getStatsJSON(String containerIdOrName) throws IOException {
        String url = urlPrefix + "containers/" + containerIdOrName + "/stats?stream=0";
        return JSON.parseObject(HttpUtil.get(url).getResult());
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
    public DockerExecResult exec(String cmd,String containerIdOrName) throws IOException {
        DockerExecResult execResult = new DockerExecResult();

        //创建exec
        String createUrl = urlPrefix + "containers/" + containerIdOrName + "/exec";
        JSONObject execCmdBody = new JSONObject();
        execCmdBody.put("AttachStdin",false);
        execCmdBody.put("AttachStdout",true);
        execCmdBody.put("AttachStderr",true);
        execCmdBody.put("DetachKeys","");
        JSONArray cmdArray = new JSONArray();
        for (String s : cmd.split(" ")) {
            if(!StringUtils.isEmpty(s)){
                cmdArray.add(s);
            }
        }
        execCmdBody.put("Cmd",cmdArray);
        String createExecResult = HttpUtil.postJSON(createUrl,execCmdBody.toJSONString()).getResult();
        String execId = JSON.parseObject(createExecResult).getString("Id");

        //执行exec
        String execUrl = urlPrefix + "exec/" + execId + "/start";
        String execBody = "{\"Detach\": false,\"Tty\": false}";
        execResult.setResult(HttpUtil.postJSON(execUrl,execBody).getResult());

        //判断执行结果
        String execResultUrl = urlPrefix + "exec/" + execId + "/json";
        JSONObject execResultJSON = JSON.parseObject(HttpUtil.get(execResultUrl).getResult());
        if(execResultJSON.getInteger("ExitCode") == 0){
            execResult.setSuccess(true);
        }

        return execResult;
    }

}
