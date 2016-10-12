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
public class IpTest {

    private static final Logger logger = LoggerFactory.getLogger(IpTest.class);

    private HttpClient httpClient;

    @Before
    public void before(){
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setConnectTimeout(1000);
        httpClientOptions.setMaxPoolSize(100);

        Vertx vertx = Vertx.vertx();
        httpClient = vertx.createHttpClient(httpClientOptions);
    }

    @Test
    public void testIp()throws Exception{
        AtomicInteger atomicInteger = new AtomicInteger(0);

        ExecutorService executorService = ExecutorUtils.newExecutors(5);
//        for(int i=0;i< 5;i++){
            executorService.execute(() -> {
                while (true){
                    try {
                        HttpClientRequest httpClientRequest = httpClient.get(8081,"172.16.101.128","/0ms0k?iP=192.168.0.1&DeviceID=aaaa-bbb-ccc1", resp -> {
                            int statusCode = resp.statusCode();
                            if(statusCode == 403){
                                //被挡
                                logger.info("请求被挡，当前请求数量为:{},时间:{}",atomicInteger.intValue(),System.currentTimeMillis());
                            }else {
                                logger.info("请求成功，当前请求数量为:{},时间:{}",atomicInteger.intValue(),System.currentTimeMillis());
                            }
                            int count = atomicInteger.incrementAndGet();
                        });


                        try {
                            TimeUnit.MILLISECONDS.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
//        }



        TimeUnit.SECONDS.sleep(600);
    }


    @Test
    public void testIpInHeader()throws Exception{

        //172.16.103.129:8081
        //172.16.103.127
        AtomicInteger atomicInteger = new AtomicInteger(0);

        ExecutorService executorService = ExecutorUtils.newExecutors(5);
//        for(int i=0;i< 5;i++){
        executorService.execute(() -> {
            while (true){
                try {
                    HttpClientRequest httpClientRequest = httpClient.get(8081,"172.16.101.128","/0ms0k", resp -> {
                        int statusCode = resp.statusCode();
                        if(statusCode == 403){
                            //被挡
                            logger.info("请求被挡，当前请求数量为:{},时间:{}",atomicInteger.intValue(),System.currentTimeMillis());
                        }else {
                            logger.info("请求成功，当前请求数量为:{},时间:{}",atomicInteger.intValue(),System.currentTimeMillis());
                        }
                        int count = atomicInteger.incrementAndGet();
                    });

                    httpClientRequest.headers().add("Ip","192.168.0.2");
                    httpClientRequest.headers().add("DeviceID","aaaa-bbb-ccc2");

                    httpClientRequest.end();

                    try {
                        TimeUnit.MILLISECONDS.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        }
        TimeUnit.SECONDS.sleep(600);
    }

}
