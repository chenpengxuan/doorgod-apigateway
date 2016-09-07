package com.ymatou.doorgod.apigateway.verticle;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tuwenjie on 2016/9/6.
 */
public class HystrixForwardReqCommand extends HystrixObservableCommand<Void> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    private HttpServerRequest httpServerReq;

    private HttpClient httpClient;

    public HystrixForwardReqCommand(HttpClient httpClient, HttpServerRequest httpServerReq) {
        super(HystrixCommandGroupKey.Factory.asKey(httpServerReq.uri()));
        this.httpClient = httpClient;
        this.httpServerReq = httpServerReq;

    }

    @Override
    protected Observable<Void> construct() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        HttpServerRequestHandler handler = new HttpServerRequestHandler(subscriber, httpClient);
                        handler.handle(httpServerReq);
                    }
                } catch (Exception e) {
                    logger.error("Failed to transfer http req {}:{}", httpServerReq.method(), httpServerReq.path(), e);
                    subscriber.onError(e);
                }
            }
        } );
    }
}
