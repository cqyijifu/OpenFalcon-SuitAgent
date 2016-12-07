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

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 插件配置文件的监听器
 * @author guqiu@yiji.com
 */
@Slf4j
public class PluginPropertiesWatcher extends Thread{

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
        WatchService watchService = WatchServiceUtil.watchModify(pluginDir);
        WatchKey key;
        while (watchService != null){
            try {
                key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    if(watchEvent.kind() == ENTRY_MODIFY){
                        String fileName = watchEvent.context() == null ? "" : watchEvent.context().toString();
                        Plugin plugin = PluginLibraryHelper.getPluginByConfigFileName(fileName);
                        if(plugin != null){
                            plugin.init(PluginLibraryHelper.getPluginConfig(plugin));
                            log.info("已完成插件{}的配置重新加载",plugin.pluginName());
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
