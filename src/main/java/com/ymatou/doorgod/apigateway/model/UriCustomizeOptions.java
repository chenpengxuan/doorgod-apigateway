package com.ymatou.doorgod.apigateway.model;

/**
 * 各URI定制项
 * Created by tuwenjie on 2016/9/7.
 */
public class UriCustomizeOptions {

    //最大并发请求数
    private int maxConcurrentReqs = -1;

    //超时时间，以毫秒计
    private int timeout = -1;

    //触发断路所需要的最小请求数
    private int requestVolumeThresholdOfCircuitBreaker;

    //在满足最小请求数的情形下，触发断路所需要的最小错误百分比
    private int errorThresholdPercentageOfCircuitBreaker;

    /**
     * 从http请求抓取deviceId的脚本。
     * 实现{@link DeviceIdFetcher}的groovy script
     */
    private String deviceIdFetcherScript;

    //TODO：降级策略

}
