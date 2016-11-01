package com.ymatou.doorgod.apigateway.integration;

import com.ymatou.performancemonitorclient.PerformanceMonitorAdvice;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by tuwenjie on 2016/11/1.
 */
@Component
public class PerformanceClient {

    @PostConstruct
    public void init( ) {
        PerformanceMonitorAdvice.init("apigateway.doorgod.iapi.ymatou.com",
                "http://monitor.iapi.ymatou.com/api/perfmon");
    }
}
