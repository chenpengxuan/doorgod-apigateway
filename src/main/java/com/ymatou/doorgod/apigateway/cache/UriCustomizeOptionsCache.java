package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.UriCustomizeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class UriCustomizeOptionsCache implements Cache {

    @Autowired
    private DefaultOptionsCache defaultOptionsCache;

    private Map<String, UriCustomizeOptions> loaded = new HashMap<String, UriCustomizeOptions>();

    @Override
    public void reload() {

    }

    public UriCustomizeOptions locate( String uri) {
        return loaded.get(uri);
    }

    public int getTimeout( String uri ) {
        UriCustomizeOptions uco = locate(uri);
        if ( uco != null && uco.getHystrixConfig() != null && uco.getHystrixConfig().getTimeout() > 0 ) {
            return uco.getHystrixConfig().getTimeout();
        }
        return defaultOptionsCache.get().getHystrixConfig().getTimeout();
    }
}
