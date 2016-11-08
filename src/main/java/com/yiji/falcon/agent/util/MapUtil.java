/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-11-08 14:59 创建
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guqiu@yiji.com
 */
public class MapUtil {

    /**
     * 获取指定Map的拥有相同Value的key集合
     * @param map
     * @param value
     * @return
     */
    public static List<Object> getSameValueKeys(Map map,Object value){
        List<Object> keys = new ArrayList<>();
        if(value != null){
            for (Object key : map.keySet()) {
                if(value.equals(map.get(key))){
                    keys.add(key);
                }
            }
        }
        return keys;
    }
}
