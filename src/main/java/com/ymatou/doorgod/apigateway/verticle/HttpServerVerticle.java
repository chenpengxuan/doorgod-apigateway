package com.ymatou.doorgod.apigateway.verticle;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

/**
 * Created by tuwenjie on 2016/9/5.
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    public void start() throws Exception {
        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

        //TODO: more settings
        //TODO: 启动时预创建连接
        HttpClientOptions httpClientOptions = new HttpClientOptions();

        HttpClient client = vertx.createHttpClient(httpClientOptions);
        HttpServer server = vertx.createHttpServer();

        if (appConfig.isEnableHystrix()) {
            server.requestHandler(httpServerReq -> {
                HystrixForwardReqCommand cmd = new HystrixForwardReqCommand(client, httpServerReq);
                cmd.observe();
            });

        } else {
            HttpServerRequestHandler handler = new HttpServerRequestHandler(null, client);
            server.requestHandler( handler );
        }

        server.listen( appConfig.getVertxServerPort());
    }
}
