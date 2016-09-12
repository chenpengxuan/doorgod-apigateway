package com.ymatou.doorgod.apigateway.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 各URI定制项
 * Created by tuwenjie on 2016/9/7.
 */
public class UriCustomizeOptions {

    private HystrixConfig hystrixConfig = new HystrixConfig();

    //统计维度key别名
    private Map<String, String> dimensionKeyAlias = new HashMap<String, String>( );

    public HystrixConfig getHystrixConfig() {
        return hystrixConfig;
    }

    public void setHystrixConfig(HystrixConfig hystrixConfig) {
        this.hystrixConfig = hystrixConfig;
    }

    public Map<String, String> getDimensionKeyAlias() {
        return dimensionKeyAlias;
    }

    public void setDimensionKeyAlias(Map<String, String> dimensionKeyAlias) {
        this.dimensionKeyAlias = dimensionKeyAlias;
    }


}
