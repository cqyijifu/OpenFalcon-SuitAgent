/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.web;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-26 13:45 创建
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author guqiu@yiji.com
 */
public class HttpServer extends Thread{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String SHUTDOWN_COMMAND = "/__SHUTDOWN__";
    private boolean shutdown = false;
    /**
     * 0 : 服务未启动
     * 1 : 服务已启动
     * -1 : 服务正在关闭
     */
    public static int status = 0;

    private int port;
    private ServerSocket serverSocket;

    public HttpServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (IOException e) {
            logger.error("web服务启动异常",e);
            status = 0;
            System.exit(0);
        }
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port, 10, InetAddress.getLocalHost());
        logger.info("Web服务启动地址:http://{}:{}",InetAddress.getLocalHost().getHostAddress(),serverSocket.getLocalPort());
        status = 1;
        while (!shutdown) {
            Socket socket;
            InputStream input;
            OutputStream output;
            try {
                socket = serverSocket.accept();

                input = socket.getInputStream();
                output = socket.getOutputStream();
                Request request = new Request(input);
                request.parse();
                Response response = new Response(output);
                response.setRequest(request);
                shutdown = SHUTDOWN_COMMAND.equals(request.getUri());
                if(shutdown){
                    status = -1;
                    response.send("Shutdown OK");
                }else{
                    response.doRequest();
                }
                socket.close();
            } catch (Exception e) {
                logger.error("Web处理异常",e);
            }
        }
        try {
            close();
        } catch (Exception e) {
            logger.error("web服务关闭异常",e);
        }
    }

    private void close() throws IOException {
        if(serverSocket != null && !serverSocket.isClosed()){
            serverSocket.close();
            status = 0;
            logger.info("Web 服务已关闭");
        }
    }

}
