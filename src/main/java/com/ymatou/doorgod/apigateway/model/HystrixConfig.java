package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class HystrixConfig extends PrintFriendliness implements Comparable<HystrixConfig> {

    public static final int DEFAULT_ERROR_THRESHOLD_PERCENTAGE_CIRCUIT_BREAKER = 50;

    //该配置所属的uri
    private String uri;

    //最大并发请求数
    private Integer maxConcurrentReqs = -1;


    //触发断路所需要的最小错误百分比
    private Integer errorThresholdPercentageOfCircuitBreaker = DEFAULT_ERROR_THRESHOLD_PERCENTAGE_CIRCUIT_BREAKER;

    //强制断路器一直断开
    private Boolean forceCircuitBreakerOpen = false;

    //强制断路器一直闭合
    private Boolean isForceCircuitBreakerClose = false;

    //降级时，返回的http响应码
    private Integer fallbackStatusCode =  -1;

    //降级时，返回的报文体
    private String fallbackBody;


    public static int getDefaultErrorThresholdPercentageCircuitBreaker() {
        return DEFAULT_ERROR_THRESHOLD_PERCENTAGE_CIRCUIT_BREAKER;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getMaxConcurrentReqs() {
        return maxConcurrentReqs;
    }

    public void setMaxConcurrentReqs(Integer maxConcurrentReqs) {
        this.maxConcurrentReqs = maxConcurrentReqs;
    }

    public Integer getErrorThresholdPercentageOfCircuitBreaker() {
        return errorThresholdPercentageOfCircuitBreaker;
    }

    public void setErrorThresholdPercentageOfCircuitBreaker(Integer errorThresholdPercentageOfCircuitBreaker) {
        this.errorThresholdPercentageOfCircuitBreaker = errorThresholdPercentageOfCircuitBreaker;
    }

    public Boolean getForceCircuitBreakerOpen() {
        return forceCircuitBreakerOpen;
    }

    public void setForceCircuitBreakerOpen(Boolean forceCircuitBreakerOpen) {
        this.forceCircuitBreakerOpen = forceCircuitBreakerOpen;
    }

    public Boolean getForceCircuitBreakerClose() {
        return isForceCircuitBreakerClose;
    }

    public void setForceCircuitBreakerClose(Boolean forceCircuitBreakerClose) {
        isForceCircuitBreakerClose = forceCircuitBreakerClose;
    }

    public Integer getFallbackStatusCode() {
        return fallbackStatusCode;
    }

    public void setFallbackStatusCode(Integer fallbackStatusCode) {
        this.fallbackStatusCode = fallbackStatusCode;
    }

    public String getFallbackBody() {
        return fallbackBody;
    }

    public void setFallbackBody(String fallbackBody) {
        this.fallbackBody = fallbackBody;
    }

    /**
     * uri越长，越靠前匹配
     * @param o
     * @return
     */
    @Override
    public int compareTo(HystrixConfig o) {
        return o.uri.length() - this.uri.length();
    }
}
