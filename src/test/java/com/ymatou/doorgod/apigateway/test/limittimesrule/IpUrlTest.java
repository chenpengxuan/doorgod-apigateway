/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.apigateway.test.limittimesrule;

import com.ymatou.doorgod.apigateway.test.utils.ExecutorUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luoshiqian 2016/10/8 15:44
 */
public class IpUrlTest {

    private static final Logger logger = LoggerFactory.getLogger(IpUrlTest.class);
    private static volatile boolean isSleepping = false;

    private HttpClient httpClient;

    @Before
    public void before(){
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setConnectTimeout(1000);
        httpClientOptions.setMaxPoolSize(100);

        Vertx vertx = Vertx.vertx();
        httpClient = vertx.createHttpClient(httpClientOptions);
    }

    /**
     * IP_URL_Prevent 60秒内 500次 限3分钏
     * @throws Exception
     */
    @Test
    public void testIpURL()throws Exception{

        //172.16.103.129:8081
        //172.16.103.127
        AtomicInteger atomicInteger = new AtomicInteger(0);


        long starttime = System.currentTimeMillis();

        while (true){
            try {

                HttpClientRequest httpClientRequest = httpClient.get(8081,"172.16.101.128","/0ms0k", resp -> {
                    int statusCode = resp.statusCode();
                    if(statusCode == 403){
                        //被挡

                        isSleepping = true;
                        logger.info("请求被挡，当前请求数量为:{},时间:{}", atomicInteger.intValue(), System.currentTimeMillis() - starttime);
                    }else {
                        if(isSleepping){
                            logger.warn("请求成功，当前请求数量为:{},时间:{}", atomicInteger.intValue(), System.currentTimeMillis() - starttime);
                            isSleepping = false;
                        }
                    }
                    int count = atomicInteger.incrementAndGet();
                });

                httpClientRequest.headers().add("Ip","192.168.0.3");

                httpClientRequest.end();
                if (isSleepping){
                    try {
                        //睡 10 秒
                        TimeUnit.MILLISECONDS.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                TimeUnit.MILLISECONDS.sleep(10);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
