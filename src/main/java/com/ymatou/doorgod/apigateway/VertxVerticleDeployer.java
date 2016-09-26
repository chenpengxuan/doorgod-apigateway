package com.ymatou.doorgod.apigateway;

import com.ymatou.doorgod.apigateway.reverseproxy.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

/**
 * 在Spring Application context启动完毕后，部署Vertx verticles
 * Created by tuwenjie on 2016/9/21.
 */
@Component
public class VertxVerticleDeployer implements ApplicationListener {

    @Autowired
    private Vertx vertx;

    private static Logger LOGGER = LoggerFactory.getLogger(VertxVerticleDeployer.class);

    private void deployVerticles() {

        CountDownLatch latch = new CountDownLatch(1);

        Throwable[] throwables = new Throwable[]{null};

        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE),
                result -> {
                    if (result.failed()) {
                        throwables[0] = result.cause();
                    }
                    latch.countDown();
                });

        //等待Verticles部署完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            throwables[0] = e;
        }

        if (throwables[0] != null) {
            throw new RuntimeException("Failed to startup ApiGateway", throwables[0]);
        }

        LOGGER.info("Succeed in startup ApiGateway");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationReadyEvent) {
            deployVerticles();
        }
    }
}