package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.UriCustomizeOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class UriCustomizeOptionsCache implements Cache {

    private Map<String, UriCustomizeOptions> loaded = new HashMap<String, UriCustomizeOptions>();

    @Override
    public void reload() {

    }

    public UriCustomizeOptions locate( String uri) {
        return loaded.get(uri);
    }
}
