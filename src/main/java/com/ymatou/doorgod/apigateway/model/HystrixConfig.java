package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class HystrixConfig {
    //最大并发请求数
    private int maxConcurrentReqs = -1;

    //超时时间，以毫秒计
    private int timeout = -1;

    //触发断路所需要的最小请求数
    private int requestVolumeThresholdOfCircuitBreaker = 20;

    //在满足最小请求数的情形下，触发断路所需要的最小错误百分比
    private int errorThresholdPercentageOfCircuitBreaker = 50;

    //强制断路器一直断开
    private boolean forceCircuitBreakerOpen = false;

    //强制断路器一直闭合
    private boolean isForceCircuitBreakerClose = false;

    public int getMaxConcurrentReqs() {
        return maxConcurrentReqs;
    }

    public void setMaxConcurrentReqs(int maxConcurrentReqs) {
        this.maxConcurrentReqs = maxConcurrentReqs;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRequestVolumeThresholdOfCircuitBreaker() {
        return requestVolumeThresholdOfCircuitBreaker;
    }

    public void setRequestVolumeThresholdOfCircuitBreaker(int requestVolumeThresholdOfCircuitBreaker) {
        this.requestVolumeThresholdOfCircuitBreaker = requestVolumeThresholdOfCircuitBreaker;
    }

    public int getErrorThresholdPercentageOfCircuitBreaker() {
        return errorThresholdPercentageOfCircuitBreaker;
    }

    public void setErrorThresholdPercentageOfCircuitBreaker(int errorThresholdPercentageOfCircuitBreaker) {
        this.errorThresholdPercentageOfCircuitBreaker = errorThresholdPercentageOfCircuitBreaker;
    }

    public boolean isForceCircuitBreakerOpen() {
        return forceCircuitBreakerOpen;
    }

    public void setForceCircuitBreakerOpen(boolean forceCircuitBreakerOpen) {
        this.forceCircuitBreakerOpen = forceCircuitBreakerOpen;
    }

    public boolean isForceCircuitBreakerClose() {
        return isForceCircuitBreakerClose;
    }

    public void setForceCircuitBreakerClose(boolean forceCircuitBreakerClose) {
        isForceCircuitBreakerClose = forceCircuitBreakerClose;
    }
}
