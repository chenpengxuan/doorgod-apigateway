package com.ymatou.doorgod.apigateway.test.filter;

import com.ymatou.doorgod.apigateway.reverseproxy.filter.DimensionKeyValueFetcher;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tuwenjie on 2016/11/4.
 */
public class DimensionKeyValueFetcherTest {

    @Test
    public void testFetchKeyManually( ) {
        Assert.assertNull(DimensionKeyValueFetcher.fetchKeyManually("/eeer/de/erere=我们deviceId=ewerwerewr", "deviceId"));
        Assert.assertEquals("ewerwerewr", DimensionKeyValueFetcher.fetchKeyManually("/eeer/de/erere=我们&deviceId=ewerwerewr&bb=e", "deviceId"));
        Assert.assertEquals("ewerwerewr", DimensionKeyValueFetcher.fetchKeyManually("/eeer/de/erere=我们&deviceId=ewerwerewr", "deviceId"));
        Assert.assertEquals("ewerwerewr", DimensionKeyValueFetcher.fetchKeyManually("/eeer/de/?deviceid=ewerwerewr", "deviceId"));

    }

}
