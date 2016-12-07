/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.web;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-26 13:54 创建
 */

import com.yiji.falcon.agent.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guqiu@yiji.com
 */
@Slf4j
public class Request {

    private InputStream input;
    /**
     * 访问URL
     */
    private String uri = null;
    /**
     * 请求头数据
     */
    private String header;
    /**
     * 访问URL的路径
     */
    private List<String> urlPath;

    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        StringBuilder request = new StringBuilder(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            log.error("request parse error",e);
            i = -1;
        }

        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        header = request.toString();
        log.debug("Request Header : \r\n {}",header);
        parseUri();
        parseUrlPath();
    }

    private void parseUri() {
        int index1, index2;
        index1 = header.indexOf(' ');
        if (index1 != -1) {
            index2 = header.indexOf(' ', index1 + 1);
            if (index2 > index1)
                uri = header.substring(index1 + 1, index2);
        }
    }

    private void parseUrlPath() {
        urlPath = new ArrayList<>();
        if(!StringUtils.isEmpty(uri)){
            for (String s : uri.split("/")) {
                s =s.trim();
                if(!StringUtils.isEmpty(s)){
                    urlPath.add(s);
                }
            }
        }
    }

    public String getUri() {
        return uri;
    }

    public List<String> getUrlPath() {
        return urlPath;
    }
}
