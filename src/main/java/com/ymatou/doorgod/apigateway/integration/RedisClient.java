package com.ymatou.doorgod.apigateway.integration;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by tuwenjie on 2016/9/9.
 */
@Component
public class RedisClient {

    @Autowired
    private AppConfig appConfig;

    private com.lambdaworks.redis.RedisClient client;

    private StatefulRedisConnection<String, String> connection;




    @PostConstruct
    public void init( ) {
        client = com.lambdaworks.redis.RedisClient.create("redis-sentinel://" + appConfig.getRedisUrl());
        connection = client.connect();
    }

    @PreDestroy
    public void destroy( ) {
        connection.close();
        client.shutdown();
    }
}
