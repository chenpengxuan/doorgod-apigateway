package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.config.BizConfig;
import com.ymatou.doorgod.apigateway.model.TargetServer;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FiltersExecutor;
import com.ymatou.doorgod.apigateway.reverseproxy.hystrix.HystrixFiltersExecutorCommand;
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
 * 反向代理的核心逻辑
 * Created by tuwenjie on 2016/9/6.
 */
public class HttpServerRequestHandler implements Handler<HttpServerRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private Subscriber<? super Void> subscriber;

    private HttpClient httpClient;

    private Vertx vertx;

    private TargetServer targetServer;

    public HttpServerRequestHandler(Subscriber<? super Void> subscriber ) {
        this.subscriber = subscriber;
        this.httpClient = VertxVerticleDeployer.httpClient;
        this.vertx = VertxVerticleDeployer.vertx;
        this.targetServer = VertxVerticleDeployer.targetServer;
    }

    @Override
    public void handle(HttpServerRequest httpServerReq) {
        LOGGER.debug("Recv:{}, by handler:{}", httpServerReq.path(), this);

        FiltersExecutor filtersExecutor = SpringContextHolder.getBean(FiltersExecutor.class);

        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

        if (appConfig.isEnableHystrix()) {

            //通过Hystrix监控FiltersExecutor性能及TPS等
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
            onCompleted();
        } else {
            HttpClientRequest forwardClientReq = httpClient.request(httpServerReq.method(), targetServer.getPort(),
                    targetServer.getHost(),
                    httpServerReq.uri(),
                    targetResp -> {
                        httpServerReq.response().setChunked(true);
                        httpServerReq.response().setStatusCode(targetResp.statusCode());
                        httpServerReq.response().headers().setAll(targetResp.headers());
                        targetResp.handler(data -> {
                            httpServerReq.response().write(data);
                        });
                        targetResp.exceptionHandler(throwable -> {
                            LOGGER.error("Failed to read target service resp {}:{}", httpServerReq.method(), httpServerReq.uri(), throwable);
                            httpServerReq.response().setStatusCode(500);
                            httpServerReq.response().end("...ApiGateway: failed to read target service response");
                            onError( throwable );
                        });
                        targetResp.endHandler((v) -> {
                            httpServerReq.response().end();
                            onCompleted();
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
                LOGGER.error("Failed to transfer reverseproxy req {}:{}", httpServerReq.method(), httpServerReq.uri(), throwable);
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
                        httpServerReq.response().write("ApiGateway:Exception in forwarding request");
                    }
                    httpServerReq.response().end();

                    onError(new Exception(throwable));
                }
            });

            httpServerReq.endHandler((v) -> forwardClientReq.end());
        }
    }


    public static void fallback( HttpServerRequest httpServerReq, String reason ) {

        HystrixConfigCache configCache = SpringContextHolder.getBean(HystrixConfigCache.class);

        HystrixConfig config = configCache.locate(httpServerReq.path().toLowerCase());

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

    public void onCompleted( ) {
        if ( subscriber != null ) {
            subscriber.onCompleted();
        }
    }

    public void onError( Throwable t ) {
        if ( subscriber != null ) {
            subscriber.onError( t );
        }
    }
}

