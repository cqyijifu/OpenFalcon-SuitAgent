/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-12-14 16:21 创建
 */

import com.yiji.falcon.agent.util.FileUtil;
import org.junit.Test;

import java.util.StringTokenizer;

/**
 * @author guqiu@yiji.com
 */
public class LogAnalysisTest {

    @Test
    public void logInfo(){
        String logPath = "/Users/QianL/Downloads/info.log";
        String content = FileUtil.getTextFileContent(logPath);
        StringTokenizer st = new StringTokenizer(content,"\n",false);
        while( st.hasMoreElements() ){
            String split = st.nextToken();
            if(split.contains("counterType=GAUGE, tags=service=standaloneJar,service.type=jmx,metrics.type=availability,agentSignName=resRoute.jar")){
                System.out.println(split);
            }
        }

    }

    @Test
    public void logDebug(){
        String logPath = "/Users/QianL/Downloads/logs/info.log";
        String content = FileUtil.getTextFileContent(logPath);
        StringTokenizer st = new StringTokenizer(content,"\n",false);
        while( st.hasMoreElements() ){
            String split = st.nextToken();
//            if(split.contains("报告Falcon") && split.contains("resRoute.jar") && split.contains("availability")){
//                System.out.println(split);
//            }
            if(split.contains("resRoute.jar") && split.contains("Variability")){
                System.out.println(split);
            }
        }

    }

}
