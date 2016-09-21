package com.ymatou.doorgod.apigateway.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by tuwenjie on 2016/9/21.
 */
public class TargetServerStarter {

    @Test
    public void startup( ) throws IOException {

        Properties props = new Properties();
        props.load(TargetServerStarter.class.getResourceAsStream("/biz.properties"));

        int targetServerPort = Integer.valueOf((String)props.get("targetWebServerPort"));

        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        server.requestHandler(request -> {
            request.response().end("ok");
        }).listen(targetServerPort);

        System.in.read();
    }
}
