package com.ymatou.doorgod.apigateway.integration;

import com.alibaba.fastjson.JSON;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.rx.RedisReactiveCommands;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
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

    public Set<Sample> getBlacklistRuleOffenders(String ruleName ) throws Exception {
        RedisReactiveCommands<String, String> commands = connection.reactive();

        Set<Sample> result = new HashSet<Sample>( );

        Observable<Long> observable = commands.smembers(
                value -> {
                    if ( result.size() < Constants.MAX_OFFENDERS ) {
                        //防止offenders占用过多内存
                        Sample sample = JSON.parseObject(value, Sample.class);
                        result.add(sample);
                    }
                },
                getRedisKeyForOffenders(ruleName));

        CountDownLatch latch = new CountDownLatch(1);

        Throwable[] throwables = new Throwable[]{null};

        observable.subscribe(
                value -> {
                },
                exception -> {
                    LOGGER.error("Failed to load blackListRule offenders for rule:{}", ruleName, exception);
                    throwables[0] = exception;
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

        if ( result.size() >= Constants.MAX_OFFENDERS ) {
            LOGGER.error("Offenders of rule:{} exceeds max acceptable count:{}", ruleName, Constants.MAX_OFFENDERS);
        }

        if ( throwables[0] != null ) {
            throw new Exception( "Failed to load offenders of rule:" + ruleName, throwables[0]);
        }

        return result;
    }


    public Map<Sample, Date> getLimitTimesRuleOffenders(String ruleName ) throws Exception {

        Map<Sample, Date> result = new HashMap<Sample, Date>( );

        RedisReactiveCommands<String, String> commands = connection.reactive();

        Observable<Long> observable = commands.hgetall(
                (key, value) -> {
                    if ( result.size() < Constants.MAX_OFFENDERS ) {
                        //防止offenders占用过多内存
                        Sample sample = JSON.parseObject(key, Sample.class);
                        //yyyyMMddHHmmss
                        Date date = Utils.parseDate( value );
                        result.put(sample, date);
                    }
                }, getRedisKeyForOffenders(ruleName));

        CountDownLatch latch = new CountDownLatch(1);

        Throwable[] throwables = new Throwable[]{null};

        observable.subscribe(
                value -> {
                },
                exception -> {
                    LOGGER.error("Failed to load limitTimesRule offenders for rule:{}", ruleName, exception);
                    throwables[0] = exception;
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

        if ( result.size() >= Constants.MAX_OFFENDERS ) {
            LOGGER.error("Offenders of rule:{} exceeds max acceptable count:{}", ruleName, Constants.MAX_OFFENDERS);
        }

        if ( throwables[0] != null ) {
            throw new Exception( "Failed to load offenders of rule:" + ruleName, throwables[0]);
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

    public StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }
}
