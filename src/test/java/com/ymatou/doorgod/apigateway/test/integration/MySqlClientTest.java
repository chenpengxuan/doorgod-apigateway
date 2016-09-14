package com.ymatou.doorgod.apigateway.test.integration;

import com.alibaba.fastjson.JSON;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.integration.RedisClient;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.test.BaseTest;
import com.ymatou.doorgod.apigateway.utils.Utils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    }


}
