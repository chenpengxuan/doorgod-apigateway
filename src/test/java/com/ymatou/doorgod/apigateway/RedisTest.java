package com.ymatou.doorgod.apigateway;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.junit.Test;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public class RedisTest {

    @Test
    public void test( ) {
        // Syntax: redis-sentinel://[password@]host[:port][,host2[:port2]][/databaseNumber]#sentinelMasterId
        RedisClient redisClient = RedisClient.create("redis-sentinel://172.16.100.104:26380,172.16.100.104:26381,172.16.100.104:26382/0#mymaster");

        StatefulRedisConnection<String, String> connection = redisClient.connect();

        System.out.println("Connected to Redis using Redis Sentinel");

        connection.close();
        redisClient.shutdown();
    }
}
