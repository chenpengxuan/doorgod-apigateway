package com.ymatou.doorgod.apigateway;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public class RedisTest {

    @Test
    public void test( ) throws ExecutionException, InterruptedException {
        // Syntax: redis-sentinel://[password@]host[:port][,host2[:port2]][/databaseNumber]#sentinelMasterId
        RedisClient redisClient = RedisClient.create("redis-sentinel://172.16.100.104:26380,172.16.100.104:26381,172.16.100.104:26382/0#mymaster");

        StatefulRedisConnection<String, String> connection = redisClient.connect();

        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.set("key", "Hello, Redis!");

        System.out.println( syncCommands.get("key"));

        RedisAsyncCommands<String, String> commands = redisClient.connect().async();

        RedisFuture<String> future = commands.get("key");
        String value = future.get();
        System.out.println(value);

        connection.close();
        redisClient.shutdown();
    }
}
