package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import com.ymatou.doorgod.apigateway.model.TargetServer;
import com.ymatou.doorgod.apigateway.reverseproxy.hystrix.HystrixForwardReqCommand;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
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

    /**
     * One vertice, one httpclient, see:
     * https://github.com/eclipse/vert.x/issues/1248
     */
    private HttpClient httpClient;


    @Override
    public void start() throws Exception {
        LOGGER.debug("start vertx verticle: {}", this);

        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

        HttpServer server = vertx.createHttpServer();

        buildHttpClient(appConfig);

        if (StringUtils.hasText(appConfig.getTargetServerWarmupUri())) {
            registerStartWarmUpHandler(appConfig);
        }

        HystrixConfigCache hystrixConfigCache = SpringContextHolder.getBean(HystrixConfigCache.class);


        server.requestHandler(httpServerReq -> {
            HystrixConfig hystrixConfig = hystrixConfigCache.locate(httpServerReq.path().toLowerCase());
            if (hystrixConfig != null) {
                HystrixForwardReqCommand cmd = new HystrixForwardReqCommand(httpServerReq, httpClient, hystrixConfig.getUri());
                cmd.observe();
            } else {
                HttpServerRequestHandler handler = new HttpServerRequestHandler(null, httpClient);
                handler.handle(httpServerReq);
            }
        });


        server.listen(appConfig.getVertxServerPort());
    }

    @Override
    public void stop() throws Exception {
        httpClient.close();
    }

    private void buildHttpClient(AppConfig appConfig) throws Exception {

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setMaxPoolSize(appConfig.getMaxHttpConnectionPoolSize());
        httpClientOptions.setLogActivity(false);


        httpClient = vertx.createHttpClient(httpClientOptions);
    }


    private void registerStartWarmUpHandler(AppConfig appConfig) {
        //用于系统启动时，预创建到目标服务器（譬如Nginx）的连接
        vertx.eventBus().consumer(VertxVerticleDeployer.ADDRESS_START_WARMUP_TARGET_SERVER, event -> {


            TargetServer ts = VertxVerticleDeployer.targetServer;

            LOGGER.info("warmming up target server {} for vertice {}...", ts, this);

            //预加载到目标服务器，譬如Nginx的连接
            for (int i = 0; i < appConfig.getInitHttpConnections(); i++) {
                HttpClientRequest req = httpClient.get(ts.getPort(), ts.getHost(),
                        appConfig.getTargetServerWarmupUri().trim(),
                        targetResp -> {
                            targetResp.endHandler(v -> {
                                LOGGER.info("Succeeded in warmming up one connection of target server {}. verticle:{}", ts, this);
                                vertx.eventBus().publish(VertxVerticleDeployer.ADDRESS_END_WARMUP_TARGET_SERVER, VertxVerticleDeployer.WARM_UP_SUCCESS_MSG);
                            });
                            targetResp.exceptionHandler(throwable -> {
                                LOGGER.error("Failed to warm up target server.", throwable);
                                vertx.eventBus().publish(VertxVerticleDeployer.ADDRESS_END_WARMUP_TARGET_SERVER, "fail");
                            });
                        });
                req.exceptionHandler(throwable -> {
                    LOGGER.error("Failed to warm up target server.", throwable);
                    vertx.eventBus().publish(VertxVerticleDeployer.ADDRESS_END_WARMUP_TARGET_SERVER, "fail");
                });
                req.end();
            }

        });
    }

}
