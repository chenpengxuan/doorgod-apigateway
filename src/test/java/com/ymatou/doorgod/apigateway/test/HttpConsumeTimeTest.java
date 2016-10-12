package com.ymatou.doorgod.apigateway.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;

/**
 * Created by tuwenjie on 2016/10/11.
 */
public class HttpConsumeTimeTest {

    @Test
    public void test( ) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000)
                .setSocketTimeout(10000).build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(10);
        cm.setMaxTotal(100);

        HttpClient httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();

        HttpPost httpPost = new HttpPost("http://172.16.103.127:8081/100ms1k");
        HttpResponse response = null;
        int loops = 1000;
        long startTime = System.currentTimeMillis();
        for ( int i=0; i<loops; i++) {

            try {

                response = httpClient.execute(httpPost);

            } finally {

                //System.out.println( response );
                httpPost.releaseConnection();
            }
        }
        System.out.println("Consumed:" + ((System.currentTimeMillis() - startTime)/loops));
    }
}
