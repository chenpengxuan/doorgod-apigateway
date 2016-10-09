package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.TargetServer;
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

        httpClient = buildHttpClient(appConfig);

        if (appConfig.isEnableHystrix()) {
            server.requestHandler(httpServerReq -> {
                HystrixForwardReqCommand cmd = new HystrixForwardReqCommand(httpServerReq, httpClient);
                cmd.observe();
            });

        } else {
            HttpServerRequestHandler handler = new HttpServerRequestHandler(null, httpClient);
            server.requestHandler( handler );
        }

        server.listen( appConfig.getVertxServerPort());
    }

    @Override
    public void stop() throws Exception {
        httpClient.close();
    }

    private HttpClient buildHttpClient(AppConfig appConfig ) throws Exception {

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setConnectTimeout(1000);
        httpClientOptions.setMaxPoolSize(appConfig.getMaxHttpConnectionPoolSize());
        httpClientOptions.setLogActivity(false);


        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);

        if (StringUtils.hasText(appConfig.getTargetServerWarmupUri())) {

            TargetServer server = VertxVerticleDeployer.targetServer;
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
