package com.ymatou.doorgod.apigateway.verticle;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.UriCustomizeOptionsCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.config.BizConfig;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

/**
 * Created by tuwenjie on 2016/9/6.
 */
public class HttpServerRequestHandler implements Handler<HttpServerRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private Subscriber<? super Void> subscriber;

    private HttpClient httpClient;

    public HttpServerRequestHandler(Subscriber<? super Void> subscriber, HttpClient httpClient) {
        this.subscriber = subscriber;
        this.httpClient = httpClient;
    }

    @Override
    public void handle(HttpServerRequest httpServerReq) {
        if ( httpServerReq.uri().equals("/warmup")) {
            httpServerReq.response().end("ok");
            return;
        }
        BizConfig bizConfig = SpringContextHolder.getBean(BizConfig.class);
        UriCustomizeOptionsCache uriCustomizeOptionsCache = SpringContextHolder.getBean(UriCustomizeOptionsCache.class);
        HttpClientRequest forwardClientReq = httpClient.request(httpServerReq.method(), bizConfig.getTargetWebServerPort(), bizConfig.getTargetWebServerHost(),
                httpServerReq.uri(),
                targetResp -> {
                    httpServerReq.response().setChunked(true);
                    httpServerReq.response().setStatusCode(targetResp.statusCode());
                    httpServerReq.response().headers().setAll(targetResp.headers());
                    targetResp.handler(data -> {
                        httpServerReq.response().write(data);
                    });
                    targetResp.exceptionHandler(throwable -> {
                        LOGGER.error("Failed to read target service resp {}:{}", httpServerReq.method(), httpServerReq.path(), throwable);
                        httpServerReq.response().setStatusCode(500);
                        httpServerReq.response().write("...ApiGateway: failed to read target service response");
                        if (subscriber != null ) {
                            subscriber.onError(throwable);
                        }
                    });
                    targetResp.endHandler((v) -> {
                        httpServerReq.response().end();

                        if (subscriber != null ) {
                            subscriber.onCompleted();
                        }

                    });
                });


        forwardClientReq.setChunked(true);
        int timeout = uriCustomizeOptionsCache.getTimeout(httpServerReq.uri());
        if ( timeout > 0 ) {
            forwardClientReq.setTimeout(timeout);
        }
        forwardClientReq.headers().setAll(httpServerReq.headers());
        httpServerReq.handler(data -> {
            forwardClientReq.write(data);
        });

        forwardClientReq.exceptionHandler(throwable -> {
            LOGGER.error("Failed to transfer http req {}:{}", httpServerReq.method(), httpServerReq.path(), throwable);
            if ( !httpServerReq.response().ended()) {
                httpServerReq.response().setChunked(true);
                if ( throwable instanceof java.net.ConnectException ) {
                    httpServerReq.response().setStatusCode(502);
                    httpServerReq.response().write("ApiGateway:failed to connect target service");
                } else if (throwable instanceof  java.util.concurrent.TimeoutException ) {
                    httpServerReq.response().setStatusCode(408);
                    httpServerReq.response().write("ApiGateway:target service timeout");
                } else {
                    httpServerReq.response().setStatusCode(500);
                    httpServerReq.response().write("ApiGateway:failed to forward request");
                }
                httpServerReq.response().end();

                if (subscriber != null ) {
                    subscriber.onError(new Exception(throwable));
                }
            }
        });

        httpServerReq.endHandler((v) -> forwardClientReq.end());

    }
}

