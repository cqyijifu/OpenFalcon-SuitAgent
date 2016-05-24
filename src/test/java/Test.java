/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/3.
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.plugins.oracle.OracleMetricsValue;
import com.yiji.falcon.agent.util.HttpUtil;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by QianLong on 16/5/3.
 */
public class Test {

    @org.junit.Test
    public void test(){
        for (Provider provider : Security.getProviders()) {
            System.out.println(provider);
            for (Map.Entry<Object, Object> entry : provider.entrySet()) {
                System.out.println("\t" + entry.getKey() + " : " + entry.getValue());
            }
        }
    }

    @org.junit.Test
    public void jdbcTest() throws SQLException, ClassNotFoundException {
        OracleMetricsValue service = new OracleMetricsValue();

//        for (Map.Entry<String, String> entry : service.getAllMetrics().entrySet()) {
//            System.out.println(entry.getKey() + " : " + entry.getValue());
//        }

    }

    @org.junit.Test
    public void esTest() throws IOException {
        String selfNodeId = "";
        String selfNodeName = "";
        String netWorkHost = "localhost";
        int port = 9200;
        String url = "http://localhost:9200/_nodes";
        String responseText = HttpUtil.get(url);
        JSONObject responseJSON = JSONObject.parseObject(responseText);
        JSONObject nodes = responseJSON.getJSONObject("nodes");
        for (Map.Entry<String, Object> entry : nodes.entrySet()) {
            String nodeId = entry.getKey();
            JSONObject nodeInfo = (JSONObject) entry.getValue();
            String nodeName = nodeInfo.getString("name");
            String http_address = nodeInfo.getString("http_address");
            if("127.0.0.1".equals(netWorkHost) || "localhost".equals(netWorkHost)){
                if(http_address.contains("127.0.0.1:" + port) || http_address.contains("localhost:" + port)){
                    selfNodeId = nodeId;
                    selfNodeName = nodeName;
                }
            }else{
                if(http_address.contains(netWorkHost + ":" + port)){
                    selfNodeId = nodeId;
                    selfNodeName = nodeName;
                }
            }
        }
        System.out.println("selfNodeId:" + selfNodeId);
        System.out.println("selfNodeName:" + selfNodeName);
    }

}
