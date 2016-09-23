package com.ymatou.doorgod.apigateway.test.filter;

import com.ymatou.doorgod.apigateway.http.filter.AbstractPreFilter;
import com.ymatou.doorgod.apigateway.http.filter.FilterContext;
import io.vertx.core.http.HttpServerRequest;

/**
 * Created by tuwenjie on 2016/9/14.
 */
public class MyFilterDemo extends AbstractPreFilter {
    @Override
    protected boolean passable(HttpServerRequest req, FilterContext context) {
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
