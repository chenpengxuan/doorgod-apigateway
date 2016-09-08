package com.ymatou.doorgod.apigateway.model;

/**
 * 各URI定制项
 * Created by tuwenjie on 2016/9/7.
 */
public class UriCustomizeOptions {

    private HystrixConfig hystrixConfig = new HystrixConfig();

    /**
     * 从http请求抓取deviceId的脚本。
     * 实现{@link DeviceIdFetcher}的groovy script
     */
    private String deviceIdFetcherScript;

    public HystrixConfig getHystrixConfig() {
        return hystrixConfig;
    }

    public void setHystrixConfig(HystrixConfig hystrixConfig) {
        this.hystrixConfig = hystrixConfig;
    }

    public String getDeviceIdFetcherScript() {
        return deviceIdFetcherScript;
    }

    public void setDeviceIdFetcherScript(String deviceIdFetcherScript) {
        this.deviceIdFetcherScript = deviceIdFetcherScript;
    }

    //TODO：降级策略

}
