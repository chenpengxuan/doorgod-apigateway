package com.ymatou.doorgod.apigateway.http.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.ymatou.doorgod.apigateway.http.HttpServerRequestHandler;
import com.ymatou.doorgod.apigateway.http.HttpServerVerticle;
import com.ymatou.doorgod.apigateway.http.hystrix.MyHystrixCommandKeyFactory;
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
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("api.ymatou"))
            .andCommandKey(MyHystrixCommandKeyFactory.asKey(httpServerReq.path())));
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

}
