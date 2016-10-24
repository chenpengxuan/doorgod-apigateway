package com.ymatou.doorgod.apigateway.reverseproxy.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FilterContext;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FiltersExecutor;
import com.ymatou.doorgod.apigateway.utils.Constants;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

/**
 * 用Hystrix上报/监控ApiGateway自身{@link FiltersExecutor}的性能
 * Created by tuwenjie on 2016/9/6.
 */
public class HystrixFiltersExecutorCommand extends HystrixObservableCommand<FilterContext> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixFiltersExecutorCommand.class);

    private HttpServerRequest httpServerReq;

    private FiltersExecutor filtersExecutor;

    public HystrixFiltersExecutorCommand(FiltersExecutor filtersExecutor, HttpServerRequest httpServerReq) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ApiGateway"))
                .andCommandKey(HystrixCommandKey.Factory.asKey(Constants.HYSTRIX_COMMAND_KEY_FILTERS_EXECUTOR))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        //filters自身永不被Hystrix熔断
                        .withCircuitBreakerEnabled(false)
                        .withExecutionTimeoutEnabled(false)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(Integer.MAX_VALUE)
                        .withRequestLogEnabled(false)));
        this.filtersExecutor = filtersExecutor;
        this.httpServerReq = httpServerReq;
    }

    @Override
    protected Observable<FilterContext> construct() {
        return Observable.create(new Observable.OnSubscribe<FilterContext>() {
            @Override
            public void call(Subscriber<? super FilterContext> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    FilterContext context = filtersExecutor.pass(httpServerReq);
                    subscriber.onNext(context);
                    subscriber.onCompleted();
                }

            }
        });
    }
}
