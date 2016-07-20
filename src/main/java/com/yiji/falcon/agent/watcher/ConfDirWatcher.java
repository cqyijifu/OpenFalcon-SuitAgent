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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 配置文件目录的监听器
 * @author guqiu@yiji.com
 */
public class ConfDirWatcher extends Thread{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        WatchService watchService;
        WatchKey key;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dir = FileSystems.getDefault().getPath(confDir);
            key = dir.register(watchService, ENTRY_MODIFY);
        } catch (IOException e) {
            logger.error("插件配置文件监听异常",e);
            return;
        }
        while (true){
            try {
                key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    if(watchEvent.kind() == ENTRY_MODIFY){
                        String fileName = watchEvent.context() == null ? "" : watchEvent.context().toString();
                        if("authorization.properties".equals(fileName)){
                            logger.info("检测到授权文件authorization.properties有改动,正在重新配置相关插件配置");
                            Set<Plugin> plugins = PluginLibraryHelper.getPluginsAboutAuthorization();
                            for (Plugin plugin : plugins) {
                                plugin.init(PluginLibraryHelper.getPluginConfig(plugin));
                                logger.info("已完成插件{}的配置重新加载",plugin.pluginName());
                            }
                        }
                    }
                }
                key.reset();
            } catch (Exception e) {
                logger.error("插件配置文件监听异常",e);
                break;
            }
        }
    }
}
