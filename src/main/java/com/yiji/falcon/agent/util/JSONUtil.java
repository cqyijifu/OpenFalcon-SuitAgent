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

import java.util.Map;

/**
 * @author guqiu@yiji.com
 */
public class JSONUtil {


    /**
     * JSON转map
     * @param map
     * 数据保存的对象
     * @param target
     * {@link com.alibaba.fastjson.JSONObject} 对象
     * @param keys
     * 若无特殊要求,传 null 即可
     * 若不为空,则返回的map中,每个key都会有传入的字符串的前缀
     * keys
     * @return
     */
    public static void jsonToMap(Map<String,Object> map,Object target, String keys){
        if(map == null){
            return;
        }
        if(target instanceof JSONObject){
            JSONObject jsonObject = (JSONObject) target;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object object = jsonObject.get(key);
                if(object instanceof JSONObject){
                    jsonToMap(map,object, StringUtils.isEmpty(keys) ? key : (keys + "." + key));
                }else if(object instanceof JSONArray){
                    jsonToMap(map,object, StringUtils.isEmpty(keys) ? key : (keys + "." + key));
                }else{
                    map.put(getKey(map,StringUtils.isEmpty(keys) ? key : (keys + "." + key),0),object);
                }
            }
        }else if(target instanceof JSONArray){
            JSONArray jsonArray = (JSONArray) target;
            for (Object object : jsonArray) {
                jsonToMap(map,object,keys);
            }
        }else{
            if(keys != null){
                map.put(getKey(map,keys,0),target);
            }
        }
    }

    private static String getKey(Map<String,Object> map,String key,int index){
        String newKey = key;
        if(index > 0){
            newKey += "." + index;
        }
        if(map.get(newKey) != null){
            newKey = getKey(map,key,++index);

        }
        return newKey;
    }
}
