/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import com.github.kevinsawicki.http.HttpRequest;
import com.yiji.falcon.agent.vo.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    
    /**
     * 发送json post请求
     * @param url
     * @param data
     * @return
     * @throws IOException
     */
    public static HttpResult postJSON(String url,String data) throws IOException {
        HttpResult result = new HttpResult();
        HttpRequest httpRequest = new HttpRequest(new URL(url),"POST")
                .connectTimeout(10000).readTimeout(10000)
                .acceptJson()
                .send(data.getBytes("utf-8"));

        result.setStatus(httpRequest.code());
        result.setResult(httpRequest.body());
        return result;
    }

    /**
     * 发送json post请求
     * @param url
     * @return
     * @throws IOException
     */
    public static HttpResult get(String url) throws IOException {
        HttpResult result = new HttpResult();

        if(!StringUtils.isEmpty(url)){
            URL requestUrl = new URL(url);
            HttpRequest httpRequest = new HttpRequest(requestUrl,"GET")
                    .connectTimeout(10000).readTimeout(10000).trustAllCerts().trustAllHosts();
            result.setStatus(httpRequest.code());
            result.setResult(httpRequest.body());
        }
        return result;
    }

    /**
     * 构造POST提交表单请求，返回响应结果
     * @param params
     * 提交的参数
     * @param address
     * 提交的地址
     * @return
     */
    public static HttpResult post(Map<String,String> params,String address) throws IOException {
        HttpResult result = new HttpResult();
        if(params == null){
            params = new HashMap<>();
        }
        if(StringUtils.isEmpty(address)){
            log.error("请求地址不能为空");
            return null;
        }
        String param = "";

        for (String key : params.keySet()) {
            if(!StringUtils.isEmpty(key)){
                param += "&" + key + "=" + params.get(key);
            }
        }

        URL requestUrl = new URL(address);
        HttpRequest httpRequest = new HttpRequest(requestUrl,"POST")
                .connectTimeout(100000).readTimeout(10000).trustAllCerts().trustAllHosts();
        httpRequest.send(param.getBytes());
        result.setStatus(httpRequest.code());
        result.setResult(httpRequest.body());
        return result;
    }

    /**
     * 用于去除字符串里的HTML代码
     * @param text
     * @return string
     */
    public static String removeHtml(String text){
        String regEx_html = "(&lt;)[^(&gt;)]+(&gt;)|<[^>]+>|[(&nbsp;)(\\n)(\\s)]";
        Pattern compile = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compile.matcher(text);
        return matcher.replaceAll("");
    }

}
