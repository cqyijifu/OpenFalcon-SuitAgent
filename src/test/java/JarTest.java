/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-10-24 14:17 创建
 */

import com.yiji.falcon.agent.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author guqiu@yiji.com
 */
public class JarTest {

    private static final String jarUnPackDir = "/Users/QianL/Desktop/SuitAgent-JarPack";
    private static final String jarFilePath = "/Users/QianL/Documents/develop/falcon-agent/Falcon-SuitAgent/target/falcon-agent.jar";

    @Test
    public void test() throws IOException {

        ZipUtil.unzip(jarFilePath,jarUnPackDir);

        Path path = Paths.get(jarUnPackDir);
        try {
            Files.walkFileTree(path,new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.toFile().getAbsolutePath();
                    if(fileName.endsWith(".class")){
                        String className = fileName.replace(jarUnPackDir,"").substring(1).replace("/",".").replace(".class","");
                        System.out.println(className);
                        System.out.println(fileName);
                    }
                    return super.visitFile(file, attrs);
                }
            });

            FileUtils.deleteDirectory(new File(jarUnPackDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
