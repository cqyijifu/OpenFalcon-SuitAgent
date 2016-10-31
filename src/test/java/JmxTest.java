/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/25.
 */

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.yiji.falcon.agent.util.CommandUtilForUnix;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by QianLong on 16/4/25.
 */
public class JmxTest {

    @Test
    public void vms(){
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor vm : vms) {
            if(vm.displayName().contains("com.yiji.falcon.agent.Agent")){
                System.out.println(vm.id());
            }
        }
    }

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

    @Test
    public void dirTest() throws IOException {

        String cmd = "lsof -p " + 5645;
        CommandUtilForUnix.ExecuteResult executeResult = CommandUtilForUnix.execWithReadTimeLimit(cmd,false,10);
        String msg = executeResult.msg;
        String[] ss = msg.split("\n");
        for (String s : ss) {
            if(s.toLowerCase().contains("cwd")){
                String[] split = s.split("\\s+");
                System.out.println(split[split.length - 1]);
            }
        }
    }

    @Test
    public void dirWalk() throws IOException {
        Path path = Paths.get("/Users/QianL/Documents/develop/falcon-agent/falcon-agent/target");
        Files.walkFileTree(path,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                String fineNameLower = fileName.toLowerCase();
                if(!fineNameLower.contains("-sources") && fineNameLower.endsWith(".jar")){
                    System.out.println(fileName);
                }

                return super.visitFile(file, attrs);
            }
        });
    }

}
