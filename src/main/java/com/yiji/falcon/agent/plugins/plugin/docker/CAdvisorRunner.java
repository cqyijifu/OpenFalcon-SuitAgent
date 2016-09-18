/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.plugin.docker;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-09-18 14:27 创建
 */

import com.yiji.falcon.agent.util.CommandUtilForUnix;
import com.yiji.falcon.agent.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @author guqiu@yiji.com
 */
public class CAdvisorRunner extends Thread{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String cadvisorPath;
    private int cadvisorPort;
    private String pid;

    public CAdvisorRunner(String cadvisorPath, int cadvisorPort) {
        this.cadvisorPath = cadvisorPath;
        this.cadvisorPort = cadvisorPort;
    }

    /**
     * 关闭cadvisor
     * @throws IOException
     */
    public void shutdownCadvisor() throws IOException {
        String cmd = "kill " + pid;
        CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(cmd,false,5, TimeUnit.SECONDS);
        if(executeResult.isSuccess){
            logger.info("关闭cadvisor成功");
        }else{
            logger.info("关闭cadvisor失败：{}",executeResult.msg);
        }
    }

    @Override
    public void run() {
        String cmd = String.format("%s -logtostderr -port=%d",cadvisorPath,cadvisorPort);
        logger.info("exec : {}","/bin/sh -c " + cmd);
        ProcessBuilder pb = new ProcessBuilder("/bin/sh","-c",cmd);
        try {
            Process process = pb.start();

            try(ByteArrayOutputStream resultOutStream = new ByteArrayOutputStream();
                InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
                InputStream processInStream = new BufferedInputStream(process.getInputStream())){

                int num;
                byte[] bs = new byte[1024];

                while ((num = errorInStream.read(bs)) != -1) {
                    resultOutStream.write(bs, 0, num);
                    print(resultOutStream);
                }

                while ((num = processInStream.read(bs)) != -1) {
                    resultOutStream.write(bs, 0, num);
                    print(resultOutStream);
                }
            }

            process.destroy();
        } catch (IOException e) {
            logger.error("",e);
        }
    }

    private void print(ByteArrayOutputStream resultOutStream) throws UnsupportedEncodingException {
        String msg = new String(resultOutStream.toByteArray(),"utf-8");
        if(StringUtils.isEmpty(pid)){
            pid = msg.split("\\s+")[2];
            logger.info("cadvisor 进程id : {}",pid);
        }
        logger.debug("cAdvisor output : {}",msg);
    }

}
