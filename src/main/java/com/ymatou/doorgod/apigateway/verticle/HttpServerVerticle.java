package com.ymatou.doorgod.apigateway.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;

/**
 * Created by tuwenjie on 2016/9/5.
 */
public class HttpServerVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        server.listen();
    }
}
