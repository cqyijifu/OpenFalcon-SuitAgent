/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import com.github.kevinsawicki.http.HttpRequest;
import com.yiji.falcon.agent.vo.HttpResult;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class HttpUtil {

    /**
     * 发送json post请求
     * @param url
     * @param data
     * @return
     * @throws IOException
     */
    public static HttpResult postJSON(String url,String data) throws IOException {
        return postJSON(url,data,10000,10000);
    }

    /**
     * 发送json post请求
     * @param url
     * @param data
     * @return
     * @throws IOException
     */
    public static HttpResult postJSON(String url,String data,int connectTimeout,int readTimeout) throws IOException {
        HttpResult result = new HttpResult();
        long start = System.currentTimeMillis();
        HttpRequest httpRequest = new HttpRequest(new URL(url),"POST")
                .connectTimeout(connectTimeout).readTimeout(readTimeout)
                .acceptJson()
                .contentType("application/json","UTF-8")
                .send(data.getBytes("UTF-8"));

        result.setStatus(httpRequest.code());
        result.setResult(httpRequest.body());
        result.setResponseTime(System.currentTimeMillis() - start);
        return result;
    }

    /**
     * 发送json post请求
     * @param url
     * @return
     * @throws IOException
     */
    public static HttpResult get(String url) throws IOException {
        return get(url,10000,10000);
    }

    /**
     * 发送json post请求
     * @param url
     * @param connectTimeout
     * @param readTimeout
     * @return
     * @throws IOException
     */
    public static HttpResult get(String url,int connectTimeout,int readTimeout) throws IOException {
        HttpResult result = new HttpResult();

        if(!StringUtils.isEmpty(url)){
            URL requestUrl = new URL(url);
            long start = System.currentTimeMillis();
            HttpRequest httpRequest = new HttpRequest(requestUrl,"GET")
                    .connectTimeout(connectTimeout).readTimeout(readTimeout).trustAllCerts().trustAllHosts();
            result.setStatus(httpRequest.code());
            result.setResult(httpRequest.body());
            result.setResponseTime(System.currentTimeMillis() - start);
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
        return post(params,address,10000,10000);
    }

    /**
     * 构造POST提交表单请求，返回响应结果
     * @param params
     * 提交的参数
     * @param address
     * 提交的地址
     * @param connectTimeout
     * @param readTimeout
     * @return
     */
    public static HttpResult post(Map<String,String> params,String address,int connectTimeout,int readTimeout) throws IOException {
        HttpResult result = new HttpResult();
        if(params == null){
            params = new HashMap<>();
        }
        if(StringUtils.isEmpty(address)){
            log.error("请求地址不能为空");
            return null;
        }
        URL requestUrl = new URL(address);
        long start = System.currentTimeMillis();
        HttpRequest httpRequest = new HttpRequest(requestUrl,"POST")
                .connectTimeout(connectTimeout).readTimeout(readTimeout).trustAllCerts().trustAllHosts();
        httpRequest.form(params,"UTF-8");
        result.setStatus(httpRequest.code());
        result.setResult(httpRequest.body());
        result.setResponseTime(System.currentTimeMillis() - start);
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
