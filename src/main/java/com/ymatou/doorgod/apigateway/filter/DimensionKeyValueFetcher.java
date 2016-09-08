package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.Utils.Utils;
import io.vertx.core.http.HttpServerRequest;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class DimensionKeyValueFetcher {

    public static String KEY_NAME_IP = "ip";

    public static String KEY_NAME_URI = "uri";

    public static String KEY_NAME_DEVICE_ID = "deviceId";

    public static String KEY_VALUE_PLACEHOLDER_EMPTY = "EMPTY_FLAG";

    public String fetch(String key, HttpServerRequest httpReq ) {
        if ( KEY_NAME_IP.equals(key)) {
            return Utils.getOriginalIp(httpReq);
        } else if (KEY_NAME_IP.equals(KEY_NAME_URI)) {
            return httpReq.uri();
        } else {
            String value = httpReq.getParam(key);
            if ( value == null || value.length() == 0 ) {
                value = httpReq.headers().get(key);
            }
            if ( value == null || value.length() == 0) {
                return KEY_VALUE_PLACEHOLDER_EMPTY;
            } else {
                return value;
            }
        }
    }
}
