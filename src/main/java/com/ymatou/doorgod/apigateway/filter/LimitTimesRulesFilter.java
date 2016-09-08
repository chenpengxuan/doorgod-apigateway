package com.ymatou.doorgod.apigateway.filter;

import io.vertx.core.http.HttpServerRequest;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class LimitTimesRulesFilter implements PreFilter {
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean pass(HttpServerRequest req) {

        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
