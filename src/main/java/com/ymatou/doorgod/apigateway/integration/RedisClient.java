package com.ymatou.doorgod.apigateway.integration;

import com.alibaba.fastjson.JSON;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.rx.RedisReactiveCommands;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tuwenjie on 2016/9/9.
 */
@Component
public class RedisClient {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

    @Autowired
    private AppConfig appConfig;

    private com.lambdaworks.redis.RedisClient client;

    private StatefulRedisConnection<String, String> connection;

    public Set<Sample> getBlacklistRuleOffenders(String ruleName ) {
        RedisReactiveCommands commands = connection.reactive();

        Set<Sample> result = new HashSet<Sample>( );

        Observable observable = commands.smembers(getRedisKeyForOffenders(ruleName));

        CountDownLatch latch = new CountDownLatch(1);

        observable.subscribe(
                value -> {
                    Sample sample = JSON.parseObject((String)value, Sample.class);
                    result.add(sample);
                },
                excepton->{
                    LOGGER.error("Failed to load blackListRule offenders for rule:{}", ruleName, excepton);
                    latch.countDown();
                },
                ()->{
                    latch.countDown();
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            //just ignore
        }

        return result;
    }


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

    public String getRedisKeyForOffenders( String ruleName ) {
        return "doorgod:" + ruleName + ":offenders";
    }
}
