/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.web;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-26 13:54 创建
 */

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.plugins.metrics.MetricsCommon;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class Response {

    private static final int BUFFER_SIZE = 1024;
    Request request;
    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void send_404() throws IOException {
        String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: 23\r\n" +
                "\r\n" +
                "<h1>File Not Found</h1>";
        output.write(errorMessage.getBytes());
    }

    public void send(String html) throws IOException {
        String errorMessage = "HTTP/1.1 200 \r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + html.length() + "\r\n" +
                "\r\n" +
                html;
        output.write(errorMessage.getBytes());
    }

    public void doRequest() throws IOException {
        List<String> urlPath = request.getUrlPath();
        if(urlPath.size() >= 1 && "mock".equals(urlPath.get(0))){
            if(urlPath.size() < 2){
                send("error! must have option");
                return;
            }
            String msg = "";
            String option = urlPath.get(1);
            if("list".equals(option)){
                msg = MetricsCommon.getMockServicesList();
            }else if(urlPath.size() != 4){
                send("<h3>error! url path must be match : /mock/{option}/{serviceType}/{serviceName}</h3>");
            }else{
                String type = urlPath.get(2);
                String server = urlPath.get(3);

                if("add".equals(option)){
                    MetricsCommon.addMockService(type,server);
                    msg = String.format("<h2>add mock server %s:%s success</h2>",type,server);
                }else if("remove".equals(option)){
                    MetricsCommon.removeMockService(type,server);
                    msg = String.format("<h2>remove mock server %s:%s success</h2>",type,server);
                }

            }
            send(msg);
        }else if(urlPath.size() >= 1 && "version".equals(urlPath.get(0))){
            send("Version " + AgentConfiguration.VERSION);
        }else{
            send_404();
        }
    }
}
