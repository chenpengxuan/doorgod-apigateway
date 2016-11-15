package com.ymatou.doorgod.apigateway.reverseproxy.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.ymatou.doorgod.apigateway.reverseproxy.HttpServerRequestHandler;
import com.ymatou.doorgod.apigateway.reverseproxy.HttpServerVerticle;
import com.ymatou.doorgod.apigateway.reverseproxy.VertxVerticleDeployer;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
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

    private String key;

    public HystrixForwardReqCommand(HttpServerRequest httpServerReq, HttpClient httpClient, String key) {
        /**
         * command Hystrix属性通过{@link DynamicHystrixPropertiesStrategy}加载
         */
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("api.ymatou")).
                andCommandKey(HystrixCommandKey.Factory.asKey(key)));
        this.httpServerReq = httpServerReq;
        this.httpClient = httpClient;
    }

    @Override
    protected Observable<Void> construct() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    try {
                        HttpServerRequestHandler handler = new HttpServerRequestHandler(subscriber, httpClient);
                        handler.handle(httpServerReq);
                    } catch (Exception e) {
                        //should never go here
                        LOGGER.error("Failed to build Hystrix observable for req:{}. {}", httpServerReq.host() + httpServerReq.path(),
                                e.getMessage(), e);
                        HttpServerRequestHandler.forceEnd(httpServerReq);
                    }
                }
            }
        });
    }

    @Override
    protected Observable<Void> resumeWithFallback() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {

                if ( HystrixForwardReqCommand.this.isResponseShortCircuited()
                        || HystrixForwardReqCommand.this.isResponseSemaphoreRejected()) {

                    //被Hystrix拦截
                    if ( VertxVerticleDeployer.appConfig.isDebugMode()) {
                        Constants.REJECT_LOGGER.info("Reject {} by Hystrix. circuitBreaker rejected:{}. maxConcurrent rejected:{}. circuitBreakerForceOpen:{} ",
                                httpServerReq.host() + httpServerReq.path().toLowerCase(),
                                HystrixForwardReqCommand.this.isResponseShortCircuited(),
                                HystrixForwardReqCommand.this.isResponseSemaphoreRejected(),
                                HystrixForwardReqCommand.this.getProperties().circuitBreakerForceOpen().get());
                    }

                    Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_REJECTED_BY_HYSTRIX, "true");
                    if (HystrixForwardReqCommand.this.isResponseShortCircuited()) {
                        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_HIT_RULE, "circuitBreaker");
                    } else if (HystrixForwardReqCommand.this.isResponseSemaphoreRejected()) {
                        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_HIT_RULE, "maxConcurrent");
                    }


                    httpServerReq.response().setStatusCode(403);
                    httpServerReq.response().setStatusMessage("Rejected by CircuitBreaker");
                }

                if (!subscriber.isUnsubscribed()) {
                    try {
                        HttpServerRequestHandler handler = new HttpServerRequestHandler(subscriber, httpClient);
                        handler.fallback(httpServerReq);
                    } catch ( Exception e) {
                        //should never go here
                        LOGGER.error("Failed to build Hystrix fallback observable for req:{}. {}", httpServerReq.host() + httpServerReq.path(),
                                e.getMessage(), e);
                        HttpServerRequestHandler.forceEnd(httpServerReq);
                    }
                }
            }
        });
    }


    /**
     * 解决信号量 最大并发动态更新
     *
     * @param commandKey
     */
    public static void removeCommandKey(String commandKey) {
        executionSemaphorePerCircuit.remove(commandKey);
    }

}
