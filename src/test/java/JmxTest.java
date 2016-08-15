/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by QianLong on 16/4/25.
 */
public class JmxTest {

    @Test
    public void jmxConnectTest() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        String host = "192.168.46.22";
        int port = 4444;
        String jmxURL = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        JMXServiceURL serviceUrl = new JMXServiceURL(jmxURL);

        Map<String,Object> map = new HashMap<>();
        String[] credentials = new String[] { "yijifu", "123456" };
        map.put(JMXConnector.CREDENTIALS, credentials);

        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, map);
        System.out.println(jmxConnector.getMBeanServerConnection());
        jmxConnector.close();
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
