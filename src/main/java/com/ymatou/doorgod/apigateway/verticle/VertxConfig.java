package com.ymatou.doorgod.apigateway.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by tuwenjie on 2016/9/13.
 */
@Configuration
public class VertxConfig {

    @Bean
    public Vertx vertx( ) {
        VertxOptions vertxOptions = new VertxOptions();
        //TODO: specific options
        return Vertx.vertx(vertxOptions);
    }
}
