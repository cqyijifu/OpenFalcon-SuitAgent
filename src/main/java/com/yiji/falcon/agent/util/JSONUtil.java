/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-08-11 17:10 创建
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guqiu@yiji.com
 */
public class JSONUtil {


    /**
     * JSON转map
     * @param target
     * {@link com.alibaba.fastjson.JSONObject} 对象
     * @param keys
     * 若无特殊要求,传 null 即可
     * 若不为空,则返回的map中,每个key都会有传入的字符串的前缀
     * keys
     * @return
     */
    public static Map<String,Object> jsonToMap(Object target, String keys){
        Map<String,Object> map = new HashMap<>();
        if(target instanceof JSONObject){
            JSONObject jsonObject = (JSONObject) target;
            for (String key : jsonObject.keySet()) {
                Object object = jsonObject.get(key);
                if(object instanceof JSONObject){
                    map.putAll(jsonToMap(object, StringUtils.isEmpty(keys) ? key : (keys + "." + key)));
                }else if(object instanceof JSONArray){
                    map.putAll(jsonToMap(object, StringUtils.isEmpty(keys) ? key : (keys + "." + key)));
                }else{
                    map.put(StringUtils.isEmpty(keys) ? key : (keys + "." + key),object);
                }
            }
        }else if(target instanceof JSONArray){
            JSONArray jsonArray = (JSONArray) target;
            for (Object object : jsonArray) {
                map.putAll(jsonToMap(object,keys));
            }
        }else{
            if(keys != null){
                map.put(keys,target);
            }
        }

        return map;
    }
}
