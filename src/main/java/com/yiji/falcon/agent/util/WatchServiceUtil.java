/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-11-21 13:54 创建
 */

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class WatchServiceUtil {

    /**
     * 监听指定路径的修改事件
     * @param path
     * @return
     */
    public static WatchService watchModify(String path){
        return watch(path,ENTRY_MODIFY);
    }

    /**
     * 监听指定路径的修改事件
     * @param path
     * @return
     */
    public static WatchService watch(String path, WatchEvent.Kind kind){
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dir = FileSystems.getDefault().getPath(path);
            dir.register(watchService, kind);
        } catch (IOException e) {
            log.error("{}监听异常",path,e);
        }
        return watchService;
    }
}
