package com.yiji.falcon.agent.plugins.oracle;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/16.
 */

import com.yiji.falcon.agent.common.AgentConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by QianLong on 16/5/16.
 */
public class OracleMeasurement {

    private static final Map<String,String> genericQueries = AgentConfiguration.INSTANCE.getOracleGenericQueries();

    /**
     * 获取指定metrics值的查询sql
     * @param metrics
     * @return
     */
    String getQuerySql(String metrics){
        return genericQueries.get(metrics);
    }

    /**
     * 获取所有的metrics
     * @return
     */
    Collection<String> getAllMetrics(){
        Collection<String> result = new ArrayList<>();
        result.addAll(genericQueries.keySet());
        return result;
    }

}
