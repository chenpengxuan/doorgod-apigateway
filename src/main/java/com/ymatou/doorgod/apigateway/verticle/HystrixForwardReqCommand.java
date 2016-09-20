package com.ymatou.doorgod.apigateway.verticle;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tuwenjie on 2016/9/6.
 */
public class HystrixForwardReqCommand extends HystrixObservableCommand<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private HttpServerRequest httpServerReq;

    private HttpClient httpClient;

    private Vertx vertx;

    public HystrixForwardReqCommand(HttpClient httpClient, HttpServerRequest httpServerReq,
                                    Vertx vertx) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(httpServerReq.path()))
            .andCommandKey(HystrixCommandKey.Factory.asKey(httpServerReq.path()))
                .andCommandPropertiesDefaults(buildCommandPropertiesSetter(httpServerReq.path()))
            );
        this.httpClient = httpClient;
        this.httpServerReq = httpServerReq;
        this.vertx = vertx;
    }

    @Override
    protected Observable<Void> construct() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        HttpServerRequestHandler handler = new HttpServerRequestHandler(subscriber, httpClient, vertx);
                        handler.handle(httpServerReq);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to transfer http req {}:{}", httpServerReq.method(), httpServerReq.path(), e);
                    subscriber.onError(e);
                }
            }
        } );
    }

    @Override
    protected Observable<Void> resumeWithFallback() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    //TODO:对于CircuitBreaker不是配置为强制断开的命令，LOGGER.error()
                    LOGGER.warn("Circuit Breaker open for uri", httpServerReq.path());
                    if (!subscriber.isUnsubscribed()) {
                        HttpServerRequestHandler.fallback(httpServerReq, "Refused by Circuit Breaker");
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to transfer http req {}:{}", httpServerReq.method(), httpServerReq.path(), e);
                    subscriber.onError(e);
                }
            }
        } );
    }

    private static HystrixCommandProperties.Setter buildCommandPropertiesSetter(String uri ) {
        //TODO: 验证是否动态生效
        HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter();

        setter.withExecutionTimeoutEnabled(false);

        setter.withRequestLogEnabled(false);

        HystrixConfigCache configCache = SpringContextHolder.getBean(HystrixConfigCache.class);

        HystrixConfig config = configCache.locate(uri);

        if ( config != null ) {
            if ( config.getMaxConcurrentReqs() != null && config.getMaxConcurrentReqs() > 0 ) {
                setter.withExecutionIsolationSemaphoreMaxConcurrentRequests(config.getMaxConcurrentReqs());
            } else {
                setter.withExecutionIsolationSemaphoreMaxConcurrentRequests(Integer.MAX_VALUE);
            }
            if (config.getForceCircuitBreakerClose() != null && config.getForceCircuitBreakerClose()) {
                setter.withCircuitBreakerForceClosed(true);
            }
            if (config.getForceCircuitBreakerOpen() != null && config.getForceCircuitBreakerOpen()) {
                setter.withCircuitBreakerForceOpen(true);
            }
            if (config.getErrorThresholdPercentageOfCircuitBreaker() != null && config.getErrorThresholdPercentageOfCircuitBreaker() > 0 ) {
                setter.withCircuitBreakerErrorThresholdPercentage(config.getErrorThresholdPercentageOfCircuitBreaker());
            }
            if (config.getTimeout() != null && config.getTimeout() > 0 ) {
                setter.withExecutionTimeoutEnabled(true);
                setter.withExecutionTimeoutInMilliseconds(config.getTimeout( ));
            }
        }

        return setter;
    }
}
