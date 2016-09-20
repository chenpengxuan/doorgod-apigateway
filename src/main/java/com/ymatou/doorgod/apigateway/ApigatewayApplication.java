package com.ymatou.doorgod.apigateway;

import com.ymatou.doorgod.apigateway.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@ComponentScan("com.ymatou.doorgod.apigateway")
@ImportResource("classpath:spring/spring-extra-beans.xml")
public class ApigatewayApplication {

    private static Logger LOGGER = LoggerFactory.getLogger(ApigatewayApplication.class);

    private static volatile boolean verticleDeployed = false;


    public static void main(String[] args) {

        //指示vertx使用logback记日志
        System.setProperty("vertx.logger-delegate-factory-class-name",
                io.vertx.core.logging.SLF4JLogDelegateFactory.class.getName());
        ConfigurableApplicationContext springContext = SpringApplication.run(ApigatewayApplication.class, args);


        Vertx vertx = springContext.getBean(Vertx.class);

        CountDownLatch latch = new CountDownLatch( 1 );

        deployVerticles(vertx, latch);

        latch.countDown();

        if ( verticleDeployed ) {
            LOGGER.info("ApiGateway startup...");
        } else {
            LOGGER.error("Failed to startup ApiGateway ");
            System.exit(0);
        }
    }

    private static void deployVerticles(Vertx vertx, CountDownLatch latch) {
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
    }

}
