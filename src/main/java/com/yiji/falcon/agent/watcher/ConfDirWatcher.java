/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.watcher;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-20 11:21 创建
 */

import com.yiji.falcon.agent.plugins.Plugin;
import com.yiji.falcon.agent.plugins.util.PluginLibraryHelper;
import com.yiji.falcon.agent.util.WatchServiceUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 配置文件目录的监听器
 * @author guqiu@yiji.com
 */
@Slf4j
public class ConfDirWatcher extends Thread{

    private String confDir;

    /**
     * 配置文件目录的监听器
     * @param confDir
     * 配置文件conf目录
     */
    public ConfDirWatcher(String confDir){
        this.confDir = confDir;
    }

    @Override
    public void run() {
        WatchService watchService = WatchServiceUtil.watchModify(confDir);
        WatchKey key;
        while (watchService != null){
            try {
                key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    if(watchEvent.kind() == ENTRY_MODIFY){
                        String fileName = watchEvent.context() == null ? "" : watchEvent.context().toString();
                        if("authorization.properties".equals(fileName)){
                            log.info("检测到授权文件authorization.properties有改动,正在重新配置相关插件配置");
                            Set<Plugin> plugins = PluginLibraryHelper.getPluginsAboutAuthorization();
                            for (Plugin plugin : plugins) {
                                plugin.init(PluginLibraryHelper.getPluginConfig(plugin));
                                log.info("已完成插件{}的配置重新加载",plugin.pluginName());
                            }
                        }
                    }
                }
                key.reset();
            } catch (Exception e) {
                log.error("插件配置文件监听异常",e);
                break;
            }
        }
    }
}
