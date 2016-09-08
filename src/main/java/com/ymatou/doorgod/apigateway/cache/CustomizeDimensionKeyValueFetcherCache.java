package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.filter.DimensionKeyValueFetcher;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class CustomizeDimensionKeyValueFetcherCache implements Cache {

    private DimensionKeyValueFetcher fetcher;

    @Override
    public void reload() {

    }

    public DimensionKeyValueFetcher getFetcher() {
        return fetcher;
    }

    public void setFetcher(DimensionKeyValueFetcher fetcher) {
        this.fetcher = fetcher;
    }
}
