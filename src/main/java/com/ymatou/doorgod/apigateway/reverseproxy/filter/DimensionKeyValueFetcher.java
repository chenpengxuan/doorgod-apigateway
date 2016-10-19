package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.google.common.collect.Sets;
import com.ymatou.doorgod.apigateway.cache.KeyAliasCache;
import com.ymatou.doorgod.apigateway.cache.UriPatternCache;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class DimensionKeyValueFetcher {

    @Autowired
    private KeyAliasCache keyAliasCache;

    @Autowired
    private UriPatternCache uriPatternCache;

    public static final String KEY_NAME_IP = "ip";

    public static final String KEY_NAME_URI = "uri";

    public String fetch(String key, HttpServerRequest httpReq ) {
        if ( KEY_NAME_IP.equals(key)) {
            return Utils.getOriginalIp(httpReq);
        } else if (KEY_NAME_URI.equals( key )) {
            return fetchUriToStatis(httpReq);
        } else {

            String alias = keyAliasCache.getAlias(httpReq.path().toLowerCase(), key);

            if (alias != null ) {
                key = alias;
            }


            String value = httpReq.getParam(key);
            if ( value == null ) {
                value = httpReq.headers().get(key);
            }
            if ( value == null ) {
                return Sample.NULL_VALUE_PLACEHOLDER;
            } else {
                return value;
            }
        }
    }

    public Sample fetch(Set<String> keys, HttpServerRequest httpReq ) {
        Sample sample = new Sample();
        keys.forEach(s -> {
            sample.addDimensionValue(s, fetch(s, httpReq));
        });
        return sample;
    }

    public String fetchUriToStatis( HttpServerRequest req) {
        String uri = req.path().toLowerCase();
        String pattern = uriPatternCache.getPattern(uri);
        if ( pattern == null ) {
            return uri;
        } else {
            //对于动态变更的uri，譬如/product/${productId},通过pattern固化
            return pattern;
        }
    }

}
