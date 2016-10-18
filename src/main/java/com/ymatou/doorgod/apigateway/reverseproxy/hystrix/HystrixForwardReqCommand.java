package com.ymatou.doorgod.apigateway.reverseproxy.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.ymatou.doorgod.apigateway.reverseproxy.HttpServerRequestHandler;
import com.ymatou.doorgod.apigateway.reverseproxy.HttpServerVerticle;
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

    private HttpServerRequestHandler handler;

    public HystrixForwardReqCommand(HttpServerRequest httpServerReq, HttpClient httpClient, String key) {
        /**
         * command Hystrix属性通过{@link DynamicHystrixPropertiesStrategy}加载
         */
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("api.ymatou")).
                andCommandKey(MyHystrixCommandKeyFactory.asKey(key)));
        this.httpServerReq = httpServerReq;
        this.httpClient = httpClient;
    }

    @Override
    protected Observable<Void> construct() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        HttpServerRequestHandler handler = new HttpServerRequestHandler(subscriber, httpClient);
                        HystrixForwardReqCommand.this.handler = handler;
                        handler.handle(httpServerReq);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to transfer reverseproxy req {}:{}", httpServerReq.method(), httpServerReq.uri(), e);
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    protected Observable<Void> resumeWithFallback() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    Constants.REJECT_LOGGER.warn("Request is rejected by Hystrix:{}. circuitBreaker rejected:{}. maxConcurrent rejected:{}. circuitBreakerForceOpen:{} ",
                            HystrixForwardReqCommand.this.httpServerReq.path().toLowerCase(),
                            HystrixForwardReqCommand.this.isResponseShortCircuited(),
                            HystrixForwardReqCommand.this.isResponseSemaphoreRejected(),
                            HystrixForwardReqCommand.this.getProperties().circuitBreakerForceOpen());

                    httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_REJECTED_BY_HYSTRIX), "true");
                    if ( HystrixForwardReqCommand.this.isResponseShortCircuited()) {
                        httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_HIT_RULE), "circuitBreaker");
                    } else if (HystrixForwardReqCommand.this.isResponseSemaphoreRejected()){
                        httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_HIT_RULE), "maxConcurrent");
                    }

                    if (!subscriber.isUnsubscribed()) {
                        handler.fallback(httpServerReq,
                                //对外统一为被断路器拦截
                                "Rejected by CircuitBreaker");
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    //should never goes here
                    LOGGER.error("Failed to do fallback process for req {}:{}", httpServerReq.method(), httpServerReq.path(), e);
                    httpServerReq.response().setStatusCode(500);
                    httpServerReq.response().write("error in fallback");
                    httpServerReq.response().end();
                    subscriber.onCompleted();
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
