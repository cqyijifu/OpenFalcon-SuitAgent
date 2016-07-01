/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;
/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-24 16:47 创建
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author guqiu@yiji.com
 */
public class PropertiesUtil {

    private final static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * 从指定properties文件获取所有的配置
     * @param file
     * @return
     */
    public static Map<String,String> getAllPropertiesByFileName(String file){
        Map<String,String> map = new HashMap<>();
        File configFile = new File(file);
        if(configFile.exists()){
            Properties pps = new Properties();
            try (FileInputStream in = new FileInputStream(configFile)){
                pps.load(in);
            } catch (IOException e) {
                log.error("配置文件配置获取失败",e);
            }
            Enumeration en = pps.propertyNames(); //得到配置文件的名字
            while(en.hasMoreElements()) {
                String strKey = (String) en.nextElement();
                String strValue = pps.getProperty(strKey);
                map.put(strKey,strValue);
            }
        }
        return map;
    }

}
