/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.yiji.falcon.agent.falcon.FalconReportObject;
import com.yiji.falcon.agent.jmx.JMXConnection;
import com.yiji.falcon.agent.zk.ZkMetricValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import javax.management.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by QianLong on 16/4/25.
 */
public class JmxTest {

//    @Test
//    public void printMetrics() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
//        String serverName = "org.apache.zookeeper.server.quorum.QuorumPeerMain";
//        MBeanServerConnection mbeanConn = JMXConnection.getMBeanConnection(serverName);
//        if(mbeanConn == null){
//            System.out.println("应用未开启");
//            return;
//        }
//        Set<ObjectInstance> beanSet = mbeanConn.queryMBeans(null, null);
//        for (ObjectInstance mbean : beanSet) {
//            System.out.println(mbean.getObjectName());
//            for (MBeanAttributeInfo mBeanAttributeInfo : mbeanConn.getMBeanInfo(mbean.getObjectName()).getAttributes()) {
//                try {
//                    System.out.println("\t" + mBeanAttributeInfo.getName() + " : " + mbeanConn.getAttribute(mbean.getObjectName(),mBeanAttributeInfo.getName()));
//                } catch (Exception ignored) {
//                }
//            }
//            System.out.println();
//        }
//    }

    @Test
    public void test() throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException, InterruptedException {
        ZkMetricValue zkMetricValue = new ZkMetricValue();
        List<FalconReportObject> requestObjectList = zkMetricValue.getConfReportObjects();
        for (FalconReportObject requestObject : requestObjectList) {
            System.out.println(requestObject.toString());
        }
        Thread.sleep(1000000);
    }

    @Test
    public void json(){
        JSONArray jsonArray = new JSONArray();
        JSONObject j1 = new JSONObject();
        j1.put("endpoint","test-endpoint");
        j1.put("metric","test-endpoint");
        j1.put("timestamp","test-endpoint");
        j1.put("step","test-endpoint");
        j1.put("value","test-endpoint");
        j1.put("counterType","test-endpoint");
        j1.put("tags","test-endpoint");
        jsonArray.put(j1);
        jsonArray.put(j1);
        jsonArray.put(j1);
        jsonArray.put(j1);
        System.out.println(jsonArray.toString());
    }

}
