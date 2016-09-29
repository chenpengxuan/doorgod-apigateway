package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.TargetServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 在Spring Application context启动完毕后，部署Vertx verticles
 * Created by tuwenjie on 2016/9/21.
 */
@Component
public class VertxVerticleDeployer {

    public static TargetServer targetServer = null;

    public static HttpClient httpClient = null;

    public static Vertx vertx = null;

    @Autowired
    private MySqlClient mySqlClient;

    @Autowired
    private AppConfig appConfig;

    private static Logger LOGGER = LoggerFactory.getLogger(VertxVerticleDeployer.class);

    public void deployVerticles() {

        //当前无需更多配置
        VertxOptions vertxOptions = new VertxOptions();
        vertx = Vertx.vertx(vertxOptions);

        try {
            targetServer = mySqlClient.locateTargetServer();
            httpClient = buildHttpClient(targetServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CountDownLatch latch = new CountDownLatch(1);

        Throwable[] throwables = new Throwable[]{null};

        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE),
                result -> {
                    if (result.failed()) {
                        throwables[0] = result.cause();
                    }
                    latch.countDown();
                });

        //等待Verticles部署完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            throwables[0] = e;
        }

        if (throwables[0] != null) {
            throw new RuntimeException("Failed to startup ApiGateway", throwables[0]);
        }

        LOGGER.info("Succeed in startup ApiGateway");
    }

    @PreDestroy
    public void destroy() {
        vertx.close();
    }


    private HttpClient buildHttpClient(TargetServer server) throws Exception {

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setConnectTimeout(1000);
        httpClientOptions.setMaxPoolSize(appConfig.getMaxHttpConnectionPoolSize());
        httpClientOptions.setLogActivity(false);


        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);


        if (StringUtils.hasText(appConfig.getTargetServerWarmupUri())) {
            //预加载到目标服务器，譬如Nginx的连接
            final Throwable[] throwableInWarmupTargetServer = {null};
            CountDownLatch latch = new CountDownLatch(appConfig.getInitHttpConnections());
            for (int i = 0; i < appConfig.getInitHttpConnections(); i++) {
                httpClient.get(server.getPort(), server.getHost(),
                        appConfig.getTargetServerWarmupUri().trim(),
                        targetResp -> {
                            targetResp.endHandler(v -> {
                                latch.countDown();
                            });
                            targetResp.exceptionHandler(throwable -> {
                                throwableInWarmupTargetServer[0] = throwable;
                                while (latch.getCount() > 0) {
                                    latch.countDown();
                                }
                            });
                        });
            }

            latch.await();

            if (throwableInWarmupTargetServer[0] != null) {
                throw new Exception("Failed to call warmup of target server. ApiGateway refuse to startup",
                        throwableInWarmupTargetServer[0]);
            }

        }

        return httpClient;
    }
}