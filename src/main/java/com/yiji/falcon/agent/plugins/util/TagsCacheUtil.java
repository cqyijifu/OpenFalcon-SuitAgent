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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author guqiu@yiji.com
 */
public class TagsCacheUtil {

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
            tags = TagsCacheUtil.getTagFromTagsCacheByAddress(addresses,tagsCache,address);
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
