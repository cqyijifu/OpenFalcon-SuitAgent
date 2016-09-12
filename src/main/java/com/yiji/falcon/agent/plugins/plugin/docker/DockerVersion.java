/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.docker;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-09-12 14:40 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class DockerVersion {

    private String dockerVersion;
    private String apiVersion;

    public String getDockerVersion() {
        return dockerVersion;
    }

    public void setDockerVersion(String dockerVersion) {
        this.dockerVersion = dockerVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
