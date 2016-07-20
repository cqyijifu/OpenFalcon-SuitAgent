/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-20 10:50 创建
 */

import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author guqiu@yiji.com
 */
public class FileWatchTest {

    @Test
    public void test() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();

        Path dir = FileSystems.getDefault().getPath("/Users/QianL/Documents/develop/falcon-agent/falcon-agent/src/main/resources/conf");
        WatchKey key = dir.register(watchService, ENTRY_MODIFY);
        while (true){
            try {
                key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    System.out.println(watchEvent.context().toString() + " : " + watchEvent.kind());
                }
                key.reset();
            } catch (Exception e) {
                break;
            }
        }
    }


}
