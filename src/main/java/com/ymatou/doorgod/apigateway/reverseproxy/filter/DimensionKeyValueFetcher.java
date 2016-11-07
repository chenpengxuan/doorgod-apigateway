package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.cache.KeyAliasCache;
import com.ymatou.doorgod.apigateway.cache.UriPatternCache;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class DimensionKeyValueFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DimensionKeyValueFetcher.class);

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

            String value = httpReq.headers().get(key);
            if ( value == null ) {
                try {
                    value = httpReq.getParam(key);
                } catch (Exception e) {
                    //线上有uri经过几层encode,导致vert.x解析失败的case
                    LOGGER.error("Failed to parse uri:{}. {}", e.getMessage(), Utils.buildFullUri(httpReq), e);
                    value = fetchKeyManually(httpReq.uri(), key);
                }
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

    /**
     * 线上有uri经过几层encode,导致vert.x解析失败的case
     * 当vert.x uri解析失败时，手动将key值扣出来
     * @param uri
     * @return
     */
    public static String fetchKeyManually( String uri, String key) {
        String lowercaseUri = uri.toLowerCase();
        String keyFlag = key.toLowerCase() + "=";
        int startIndex = lowercaseUri.indexOf(keyFlag);
        if ( startIndex > 0 &&
                (uri.charAt(startIndex-1) == '?' || uri.charAt(startIndex-1) == '&')) {
            int endIndex = lowercaseUri.indexOf("&", startIndex);
            if ( endIndex > 0) {
                return uri.substring(startIndex + keyFlag.length(), endIndex);
            } else {
                return uri.substring(startIndex + keyFlag.length());
            }
        }
        return null;
    }

}
