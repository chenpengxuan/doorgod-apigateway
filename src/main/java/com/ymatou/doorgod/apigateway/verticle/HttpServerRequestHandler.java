package com.ymatou.doorgod.apigateway.verticle;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.config.BizConfig;
import com.ymatou.doorgod.apigateway.filter.FiltersExecutor;
import com.ymatou.doorgod.apigateway.filter.HystrixFiltersExecutorCommand;
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
        if (httpServerReq.uri().equals("/warmup")) {
            httpServerReq.response().end("ok");
            return;
        } else if (httpServerReq.uri().equals("/version")) {
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
            HystrixFiltersExecutorCommand filtersExecutorCommand = new HystrixFiltersExecutorCommand(filtersExecutor, httpServerReq);
            filtersExecutorCommand.toObservable().subscribe(passed -> {
                process(httpServerReq, passed);
            });
        } else {
            boolean passed = filtersExecutor.pass(httpServerReq);
            process(httpServerReq, passed);
        }

    }

    private void process( HttpServerRequest httpServerReq, boolean passed ) {
        if (!passed) {
            httpServerReq.response().setStatusCode(403);
            httpServerReq.response().end("ApiGateway: request is forbidden.");
        } else {
            BizConfig bizConfig = SpringContextHolder.getBean(BizConfig.class);
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
}

