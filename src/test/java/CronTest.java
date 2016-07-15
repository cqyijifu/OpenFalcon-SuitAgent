/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/4/28.
 */

import com.yiji.falcon.agent.util.CronUtil;
import org.junit.Test;

/**
 * Created by QianLong on 16/4/28.
 */
public class CronTest {

    @Test
    public void test(){
        System.out.println("30 "+ CronUtil.getCronBySecondScheduler(30));
        System.out.println("59 " + CronUtil.getCronBySecondScheduler(59));
        System.out.println("60 " +CronUtil.getCronBySecondScheduler(60));
        System.out.println("61 " + CronUtil.getCronBySecondScheduler(61));
        System.out.println("100 " + CronUtil.getCronBySecondScheduler(100));
        System.out.println("360 " + CronUtil.getCronBySecondScheduler(360));
        System.out.println("3600 " + CronUtil.getCronBySecondScheduler(3600));
        System.out.println("3630 " + CronUtil.getCronBySecondScheduler(3630));
        System.out.println("4000 " + CronUtil.getCronBySecondScheduler(4000));
    }

}
