package com.ymatou.doorgod.apigateway.integration;

import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.performancemonitorclient.PerformanceMonitorAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Created by tuwenjie on 2016/11/1.
 */
@Component
public class PerformanceClient {

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    public void init( ) {
        String defaultUrl = "http://monitor.iapi.ymatou.com/api/perfmon";
        if (StringUtils.hasText(appConfig.getPerformanceServerUrl())) {
            defaultUrl = appConfig.getPerformanceServerUrl().trim();
        }
        PerformanceMonitorAdvice.init("apigateway.doorgod.iapi.ymatou.com",
                defaultUrl);
    }
}
