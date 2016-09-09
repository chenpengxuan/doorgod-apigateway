package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.Utils.Utils;
import com.ymatou.doorgod.apigateway.model.Sample;
import io.vertx.core.http.HttpServerRequest;

import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class DimensionKeyValueFetcher {

    public static String KEY_NAME_IP = "ip";

    public static String KEY_NAME_URI = "uri";

    public static String KEY_NAME_DEVICE_ID = "deviceId";

    public String fetch(String key, HttpServerRequest httpReq ) {
        if ( KEY_NAME_IP.equals(key)) {
            return Utils.getOriginalIp(httpReq);
        } else if (KEY_NAME_IP.equals(KEY_NAME_URI)) {
            return httpReq.uri();
        } else {
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
}
