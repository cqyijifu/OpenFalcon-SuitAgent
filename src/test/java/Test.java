/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/5/3.
 */

import com.yiji.falcon.agent.plugins.oracle.OracleMetricsValue;

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

}
