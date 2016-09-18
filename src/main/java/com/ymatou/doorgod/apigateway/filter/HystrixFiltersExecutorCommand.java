package com.ymatou.doorgod.apigateway.filter;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tuwenjie on 2016/9/6.
 */
public class HystrixFiltersExecutorCommand extends HystrixObservableCommand<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixFiltersExecutorCommand.class);

    private HttpServerRequest httpServerReq;

    private FiltersExecutor filtersExecutor;

    public HystrixFiltersExecutorCommand(FiltersExecutor filtersExecutor, HttpServerRequest httpServerReq ) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("filterExecutor"))
            .andCommandKey(HystrixCommandKey.Factory.asKey("filterExecutor"))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withCircuitBreakerEnabled(false)
                .withExecutionIsolationSemaphoreMaxConcurrentRequests(Integer.MAX_VALUE)));
        this.filtersExecutor = filtersExecutor;
        this.httpServerReq = httpServerReq;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        boolean result = filtersExecutor.pass(httpServerReq);
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    logger.error("Failed to execute filters for http req {}:{}", httpServerReq.method(), httpServerReq.path(), e);
                    subscriber.onError(e);
                }
            }
        } );
    }
}
