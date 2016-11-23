/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.plugins.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-11-16 09:51 创建
 */

import com.yiji.falcon.agent.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guqiu@yiji.com
 */
public class CacheUtil {

    /**
     * 获取缓存中，超时的key
     * @param map
     * @return
     */
    public static List<String> getTimeoutCacheKeys(ConcurrentHashMap<String,String> map){
        List<String> keys = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            long cacheTime = getCacheTime(entry.getValue());
            if(cacheTime == 0){
                keys.add(entry.getKey());
            }else if(now - cacheTime >= 2 * 24 * 60 * 60 * 1000){
                //超时2天
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * 设置缓存值，添加时间戳
     * @param value
     * @return
     */
    public static String setCacheValue(String value){
        if(!StringUtils.isEmpty(value)){
            return String.format("@%d@%s",System.currentTimeMillis(),value);
        }
        return value;
    }

    /**
     * 获取缓存值，去除时间戳
     * @param value
     * @return
     */
    public static String getCacheValue(String value){
        if(!StringUtils.isEmpty(value)){
            return value.replaceAll("@\\d*@","");
        }
        return value;
    }

    /**
     * 获取缓存值中的时间戳
     * @param value
     * @return
     */
    private static long getCacheTime(String value){
        try {
            if(value != null){
                return Long.parseLong(value.replace(getCacheValue(value),"").replace("@",""));
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    /**
     * 从行默认tag缓存中获取tag
     * @param addresses
     * @param tagsCache
     * @param address
     * @return
     */
    public static Map<String,String> getTags(Map<String,String> addresses,Map<String,String> tagsCache,String address){
        Map<String,String> map = new HashMap<>();
        String adds;
        String tags;
        if(address.endsWith("[]")){
            // ip[] 形式的，设置 空串 tag
            tags = "";
            adds = address.replace("[]","");
        }else if (address.contains("[") && address.contains("]")){
            // ip[tag=tagValue] 形式的，设置它单独的tag
            adds = address.substring(0,address.indexOf("["));
            tags = address.substring(address.indexOf("[") + 1,address.lastIndexOf("]"));
            tags = tags.replace(";",",");
        }else{
            // ip 形式的，获取 行默认tag
            tags = CacheUtil.getTagFromTagsCacheByAddress(addresses,tagsCache,address);
            adds = address;
        }
        map.put("adds",adds);
        map.put("tags",tags);
        return map;
    }

    private static String getTagFromTagsCacheByAddress(Map<String, String> addresses, Map<String, String> tagsCache, String address){
        if(!StringUtils.isEmpty(address)){
            Optional<String> cacheKey =addresses.keySet().stream()
                    .filter(key -> addresses.get(key) != null && addresses.get(key).contains(address)).findFirst();
            if(cacheKey.isPresent()){
                return tagsCache.get(cacheKey.get());
            }
        }
        return null;
    }

    /**
     * 初始化行默认tag缓存
     * @param addresses
     * @param tagsCache
     */
    public static void initTagsCache(Map<String,String> addresses,Map<String,String> tagsCache){
        addresses.keySet().forEach(key -> {
            String value = addresses.get(key);
            String firstAddress = value.split(",")[0];
            if (!firstAddress.endsWith("[]") && firstAddress.contains("[") && firstAddress.contains("]")) {
                //添加 行默认 的 tag cache
                String tags = firstAddress.substring(firstAddress.indexOf("[") + 1, firstAddress.lastIndexOf("]"));
                tags = tags.replace(";", ",");
                tagsCache.put(key, tags);
            }
        });
    }
}
