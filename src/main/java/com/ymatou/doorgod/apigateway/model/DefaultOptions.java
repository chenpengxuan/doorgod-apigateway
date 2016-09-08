package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class DefaultOptions {
    private HystrixConfig hystrixConfig = new HystrixConfig();

    public HystrixConfig getHystrixConfig() {
        return hystrixConfig;
    }

    public void setHystrixConfig(HystrixConfig hystrixConfig) {
        this.hystrixConfig = hystrixConfig;
    }
}
