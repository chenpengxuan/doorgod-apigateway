package com.ymatou.doorgod.apigateway.test.integration;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.integration.MongodbClient;
import com.ymatou.doorgod.apigateway.model.RuleTypeEnum;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.test.BaseTest;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by tuwenjie on 2016/9/14.
 */
public class MongodbClientTest extends BaseTest {

    @Autowired
    private MongodbClient client;

    @Test
    public void test( ) throws Exception {

        Sample sample = new Sample( );
        sample.addDimensionValue("ip", Utils.localIp());

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("dimensionValues", new JsonObject().put("ip",  Utils.localIp()));

        client.mongo().insert(Constants.COLLECTION_LIMIT_TIMES_RULE_OFFENDER,
                new JsonObject().put("ruleName", "test")
                    .put("sample", jsonObject)
                    .put("releaseDate", Long.valueOf(Utils.getCurrentTimeStr()) + 10000),
                res -> {}
        );

        Thread.sleep(500);

        System.out.println( client.loadRuleOffenders("test", RuleTypeEnum.LimitTimesRule));
    }


}
