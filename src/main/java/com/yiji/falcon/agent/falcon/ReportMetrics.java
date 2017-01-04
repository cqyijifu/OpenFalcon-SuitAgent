/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.falcon;

import com.yiji.falcon.agent.config.AgentConfiguration;
import com.yiji.falcon.agent.util.DateUtil;
import com.yiji.falcon.agent.util.HttpUtil;
import com.yiji.falcon.agent.util.StringUtils;
import com.yiji.falcon.agent.vo.HttpResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class ReportMetrics {

    private static final Logger log = LoggerFactory.getLogger(ReportMetrics.class);

    /**
     * 推送数据到falcon
     * @param falconReportObjectList
     */
    public static void push(Collection<FalconReportObject> falconReportObjectList){
        if(falconReportObjectList != null && !falconReportObjectList.isEmpty()){
            JSONArray jsonArray = new JSONArray();
            long timestamp = 0;
            for (FalconReportObject falconReportObject : falconReportObjectList) {
                if(!isValidTag(falconReportObject)){
                    log.error("报告对象的tag为空,此metrics将不允上报:{}",falconReportObject.toString());
                    continue;
                }
                timestamp = falconReportObject.getTimestamp();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("endpoint",falconReportObject.getEndpoint());
                jsonObject.put("metric",falconReportObject.getMetric());
                jsonObject.put("timestamp",falconReportObject.getTimestamp());
                jsonObject.put("step",falconReportObject.getStep());
                jsonObject.put("value",falconReportObject.getValue());
                jsonObject.put("counterType",falconReportObject.getCounterType());
                jsonObject.put("tags",falconReportObject.getTags() == null ? "" : falconReportObject.getTags());
                jsonArray.put(jsonObject);
            }
            String time = DateUtil.getFormatDateTime(new Date(timestamp * 1000));
            log.debug("报告Falcon({}) : [{}]",time,jsonArray.toString());
            HttpResult result;
            try {
                result = HttpUtil.postJSON(AgentConfiguration.INSTANCE.getAgentPushUrl(),jsonArray.toString());
            } catch (Exception e) {
                log.error("metrics push异常,检查Falcon组件是否运行正常",e);
                return;
            }
            log.info("push回执: {}" , result);
        }else {
            log.info("push对象为null");
        }
    }

    /**
     * 推送数据到falcon
     * @param falconReportObject
     */
    public static void push(FalconReportObject falconReportObject){
        if(!isValidTag(falconReportObject)){
            log.error("报告对象的tag为空,此metrics将不允上报:{}",falconReportObject.toString());
            return;
        }

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("endpoint",falconReportObject.getEndpoint());
        jsonObject.put("metric",falconReportObject.getMetric());
        jsonObject.put("timestamp",falconReportObject.getTimestamp());
        jsonObject.put("step",falconReportObject.getStep());
        jsonObject.put("value",falconReportObject.getValue());
        jsonObject.put("counterType",falconReportObject.getCounterType());
        jsonObject.put("tags",falconReportObject.getTags() == null ? "" : falconReportObject.getTags());
        jsonArray.put(jsonObject);
        log.debug("报告Falcon : [{}]",jsonArray.toString());
        HttpResult result;
        try {
            result = HttpUtil.postJSON(AgentConfiguration.INSTANCE.getAgentPushUrl(),jsonArray.toString());
        } catch (Exception e) {
            log.error("metrics push异常,检查Falcon组件是否运行正常",e);
            return;
        }
        log.info("push回执: {}" , result);
    }

    /**
     * 判断tag是否有效
     * @param falconReportObject
     * @return
     */
    private static boolean isValidTag(FalconReportObject falconReportObject){
        return falconReportObject != null && !StringUtils.isEmpty(falconReportObject.getTags());
    }

}
