package com.ymatou.doorgod.apigateway;

import com.ymatou.doorgod.apigateway.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tuwenjie on 2016/9/21.
 */
@Component
public class VerticleDeployer implements ApplicationListener {

    @Autowired
    private Vertx vertx;

    private static Logger LOGGER = LoggerFactory.getLogger(VerticleDeployer.class);

    private static volatile boolean verticleDeployed = false;



    private void deployVerticles( ) throws Exception {

        CountDownLatch latch = new CountDownLatch( 1 );

        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE),
                result -> {
                    if (result.succeeded()) {
                        verticleDeployed = true;
                    } else {
                        LOGGER.error("Failed to startup HttpServerVerticle", result.cause());
                    }
                    latch.countDown();
                });

        latch.await();

        if ( verticleDeployed ) {
            LOGGER.info("ApiGateway startup...");
        } else {
            throw new RuntimeException("Failed to startup ApiGateway ");
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if ( applicationEvent instanceof ApplicationReadyEvent ) {
            try {
                deployVerticles();
            } catch (Exception e) {
                LOGGER.error("Failed to startup", e);
                System.exit(0);
            }
        }
    }
}
