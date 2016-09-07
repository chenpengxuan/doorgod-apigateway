package com.ymatou.doorgod.apigateway;

import com.ymatou.doorgod.apigateway.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sun.security.provider.certpath.Vertex;

import java.io.IOException;

/**
 * Created by tuwenjie on 2016/9/6.
 */
public class DeployVerticleTest extends BaseTest {

    @Test
    public void test( ) throws IOException {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions();
        options.setInstances(2);
        vertx.deployVerticle(HttpServerVerticle.class.getName(), options);
        System.in.read();
    }
}
