package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.filter.DimensionKeyValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class CustomizeDimensionKeyValueFetcherCache implements Cache {

    private DimensionKeyValueFetcher customizeFetcher;

    @Autowired
    private DimensionKeyValueFetcher defaultFetcher;

    @Override
    public void reload() {

    }

    public DimensionKeyValueFetcher getFetcher() {
        return customizeFetcher == null ? defaultFetcher : customizeFetcher;
    }

}
