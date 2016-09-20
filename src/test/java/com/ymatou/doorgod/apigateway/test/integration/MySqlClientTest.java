package com.ymatou.doorgod.apigateway.test.integration;

import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by tuwenjie on 2016/9/14.
 */
public class MySqlClientTest extends BaseTest {

    @Autowired
    private MySqlClient mySqlClient;

    @Test
    public void test( ) throws Exception {
        mySqlClient.loadAllRules();
        mySqlClient.loadCustomizeFilters();

        System.out.println( mySqlClient.loadHystrixConfigs());

    }


}
