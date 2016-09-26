package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.config.BizConfig;
import com.ymatou.doorgod.apigateway.reverseproxy.hystrix.HystrixForwardReqCommand;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.concurrent.CountDownLatch;

/**
 * Created by tuwenjie on 2016/9/5.
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    //多个verticle实例共用一个httpClient
    private static volatile HttpClient httpClient;

    @Override
    public void start() throws Exception {
        LOGGER.debug("start vertx verticle: {}", this);

        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

        initHttpClientIfNeed();

        HttpServer server = vertx.createHttpServer();

        if (appConfig.isEnableHystrix()) {
            server.requestHandler(httpServerReq -> {
                HystrixForwardReqCommand cmd = new HystrixForwardReqCommand(httpClient, httpServerReq, vertx);
                cmd.observe();
            });

        } else {
            HttpServerRequestHandler handler = new HttpServerRequestHandler(null, httpClient, vertx);
            server.requestHandler( handler );
        }

        server.listen( appConfig.getVertxServerPort());
    }

    public void initHttpClientIfNeed( ) throws Exception {
        if ( httpClient == null ) {
            synchronized (HttpServerVerticle.class) {
                if ( httpClient == null ) {
                    AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);
                    BizConfig bizConfig = SpringContextHolder.getBean(BizConfig.class);

                    HttpClientOptions httpClientOptions = new HttpClientOptions();
                    httpClientOptions.setConnectTimeout(1000);
                    httpClientOptions.setMaxPoolSize(appConfig.getMaxHttpConnectionPoolSize());
                    httpClientOptions.setLogActivity(false);


                    httpClient = vertx.createHttpClient(httpClientOptions);


                    if (StringUtils.hasText(appConfig.getTargetServerWarmupUri())){
                        //预加载到目标服务器，譬如Nginx的连接
                        final Throwable[] throwableInWarmupTargetServer = {null};
                        CountDownLatch latch = new CountDownLatch(appConfig.getInitHttpConnections());
                        for ( int i=0; i<appConfig.getInitHttpConnections(); i++) {

                            //TODO: targetServer/port 通过数据库配置. 一个实例一个<targetServer, port>
                            httpClient.get(bizConfig.getTargetWebServerPort(), bizConfig.getTargetWebServerHost(),
                                    appConfig.getTargetServerWarmupUri().trim(),
                                    targetResp -> {
                                        targetResp.endHandler(v -> {
                                            latch.countDown();
                                        });
                                        targetResp.exceptionHandler(throwable -> {
                                            throwableInWarmupTargetServer[0] = throwable;
                                            while (latch.getCount()>0) {
                                                latch.countDown();
                                            }
                                        });
                                    });
                        }

                        latch.await();

                        if ( throwableInWarmupTargetServer[0] != null ) {
                            throw new Exception("Failed to call warmup of target server. ApiGateway refuse to startup",
                                    throwableInWarmupTargetServer[0]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if ( httpClient != null ) {
            synchronized ( HttpServerVerticle.class ) {
                if ( httpClient != null ) {
                    httpClient.close();
                    httpClient = null;
                }
            }
        }
    }
}
