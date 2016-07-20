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

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 插件配置文件的监听器
 * @author guqiu@yiji.com
 */
public class PluginPropertiesWatcher extends Thread{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String pluginDir;

    /**
     * 插件配置文件的监听器
     * @param pluginDir
     * 插件目录
     */
    public PluginPropertiesWatcher(String pluginDir){
        this.pluginDir = pluginDir;
    }

    @Override
    public void run() {
        WatchService watchService;
        WatchKey key;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dir = FileSystems.getDefault().getPath(pluginDir);
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
                        Plugin plugin = PluginLibraryHelper.getPluginByConfigFileName(fileName);
                        if(plugin != null){
                            plugin.init(PluginLibraryHelper.getPluginConfig(plugin));
                            logger.info("已完成插件{}的配置重新加载",plugin.pluginName());
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
