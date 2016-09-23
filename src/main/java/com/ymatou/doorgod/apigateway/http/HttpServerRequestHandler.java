package com.ymatou.doorgod.apigateway.http;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.config.BizConfig;
import com.ymatou.doorgod.apigateway.http.filter.FiltersExecutor;
import com.ymatou.doorgod.apigateway.http.filter.HystrixFiltersExecutorCommand;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
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

    private Vertx vertx;

    public HttpServerRequestHandler(Subscriber<? super Void> subscriber, HttpClient httpClient, Vertx vertx) {
        this.subscriber = subscriber;
        this.httpClient = httpClient;
        this.vertx = vertx;
    }

    @Override
    public void handle(HttpServerRequest httpServerReq) {
        LOGGER.debug("Recv:{}", httpServerReq.path());
        if (httpServerReq.path().equals("/warmup")) {
            httpServerReq.response().end("ok");
            return;
        } else if (httpServerReq.path().equals("/version")) {
            vertx.fileSystem().readFile(HttpServerRequestHandler.class.getResource("/version.txt").getFile(),
                    result -> {
                        if (result.succeeded()) {
                            httpServerReq.response().end(result.result());
                        }
                    });
            return;
        }

        FiltersExecutor filtersExecutor = SpringContextHolder.getBean(FiltersExecutor.class);

        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

        if (appConfig.isEnableHystrix()) {

            //通过Hystrix监控FiltersExecutor性能及Gateway接收的所有请求
            boolean[] passFilters = new boolean[]{false};
            HystrixFiltersExecutorCommand filtersExecutorCommand = new HystrixFiltersExecutorCommand(filtersExecutor, httpServerReq);
            filtersExecutorCommand.toObservable().subscribe(passed -> {
                passFilters[0] = passed;
            });

            process(httpServerReq, passFilters[0]);
        } else {
            boolean passed = filtersExecutor.pass(httpServerReq);
            process(httpServerReq, passed);
        }

    }

    private void  process( HttpServerRequest httpServerReq, boolean passed ) {
        if (!passed) {
            fallback(httpServerReq, "Refused by filters");
            if (subscriber != null ) {
                subscriber.onCompleted();
            }
        } else {
            BizConfig bizConfig = SpringContextHolder.getBean(BizConfig.class);
            HttpClientRequest forwardClientReq = httpClient.request(httpServerReq.method(), bizConfig.getTargetWebServerPort(), bizConfig.getTargetWebServerHost(),
                    httpServerReq.path(),
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
                            if (subscriber != null) {
                                subscriber.onError(throwable);
                            }
                        });
                        targetResp.endHandler((v) -> {
                            httpServerReq.response().end();

                            if (subscriber != null) {
                                subscriber.onCompleted();
                            }

                        });
                    });


            forwardClientReq.setChunked(true);
            forwardClientReq.headers().setAll(httpServerReq.headers());

            /**
             * 对于明确设置了超时时间的uri,设定超时时间
             */
            HystrixConfigCache configCache = SpringContextHolder.getBean(HystrixConfigCache.class);
            HystrixConfig config = configCache.locate(httpServerReq.path());
            if ( config != null && config.getTimeout() != null && config.getTimeout() > 0 ) {
                forwardClientReq.setTimeout(config.getTimeout());
            }


            httpServerReq.handler(data -> {
                forwardClientReq.write(data);
            });

            forwardClientReq.exceptionHandler(throwable -> {
                LOGGER.error("Failed to transfer http req {}:{}", httpServerReq.method(), httpServerReq.path(), throwable);
                if (!httpServerReq.response().ended()) {
                    httpServerReq.response().setChunked(true);
                    if (throwable instanceof java.net.ConnectException) {
                        httpServerReq.response().setStatusCode(502);
                        httpServerReq.response().write("ApiGateway:failed to connect target service");
                    } else if (throwable instanceof java.util.concurrent.TimeoutException) {
                        httpServerReq.response().setStatusCode(408);
                        httpServerReq.response().write("ApiGateway:target service timeout");
                    } else {
                        httpServerReq.response().setStatusCode(500);
                        httpServerReq.response().write("ApiGateway:failed to forward request");
                    }
                    httpServerReq.response().end();

                    if (subscriber != null) {
                        subscriber.onError(new Exception(throwable));
                    }
                }
            });

            httpServerReq.endHandler((v) -> forwardClientReq.end());
        }
    }


    public static void fallback( HttpServerRequest httpServerReq, String reason ) {

        HystrixConfigCache configCache = SpringContextHolder.getBean(HystrixConfigCache.class);

        HystrixConfig config = configCache.locate(httpServerReq.path());

        if (config != null && config.getFallbackStatusCode() != null
                && config.getFallbackStatusCode() > 0 ) {
            httpServerReq.response().setStatusCode(config.getFallbackStatusCode());
            if ( config.getFallbackBody() != null ) {
                httpServerReq.response().end(config.getFallbackBody());
            } else {
                httpServerReq.response().end();
            }
        } else {
            httpServerReq.response().setStatusCode(403);
            httpServerReq.response().end("ApiGateway: request is forbidden." + reason );
        }

    }
}

