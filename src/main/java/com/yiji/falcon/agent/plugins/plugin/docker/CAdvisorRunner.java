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

/**
 * @author guqiu@yiji.com
 */
public class CAdvisorRunner extends Thread{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String cAdvisorPath;
    private int cAdvisorPort;
    private String pid;

    public CAdvisorRunner(String cAdvisorPath, int cAdvisorPort) {
        this.cAdvisorPath = cAdvisorPath;
        this.cAdvisorPort = cAdvisorPort;
        this.setDaemon(true);
    }

    /**
     * 关闭cadvisor
     * @throws IOException
     */
    public void shutdownCAdvisor() throws IOException {
        String cmd = "kill " + pid;
        CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(cmd,false,5);
        if(executeResult.isSuccess){
            logger.info("关闭cAdvisor成功({})",cmd);
        }else{
            logger.info("关闭cAdvisor失败({})：{}",cmd,executeResult.msg);
        }
    }

    @Override
    public void run() {
        String cmd = String.format("%s -logtostderr -port=%d", cAdvisorPath, cAdvisorPort);
        logger.debug("启动内置cAdvisor服务 : {}","/bin/sh -c " + cmd);
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
            logger.info("cAdvisor 进程id : {}",pid);
        }
        logger.debug("cAdvisor output : {}",msg);
    }

}
