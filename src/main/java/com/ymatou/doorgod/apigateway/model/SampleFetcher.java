package com.ymatou.doorgod.apigateway.model;

import io.vertx.core.http.HttpServerRequest;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public interface SampleFetcher {

    Sample fetch(HttpServerRequest req);
}
