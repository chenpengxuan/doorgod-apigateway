package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.VertxVerticleDeployer;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.config.BizConfig;
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


    @Override
    public void start() throws Exception {
        LOGGER.debug("start vertx verticle: {}", this);

        HttpClient httpClient = (HttpClient) this.config().getValue(VertxVerticleDeployer.CONFIG_NAME_HTTP_CLIENT);

        TargetServer targetServer = (TargetServer) this.config().getValue(VertxVerticleDeployer.CONFIG_NAME_TARGET_SERVER);

        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

        HttpServer server = vertx.createHttpServer();

        if (appConfig.isEnableHystrix()) {
            server.requestHandler(httpServerReq -> {
                HystrixForwardReqCommand cmd = new HystrixForwardReqCommand(httpClient, httpServerReq, vertx, targetServer);
                cmd.observe();
            });

        } else {
            HttpServerRequestHandler handler = new HttpServerRequestHandler(null, httpClient, vertx, targetServer);
            server.requestHandler( handler );
        }

        server.listen( appConfig.getVertxServerPort());
    }

}
