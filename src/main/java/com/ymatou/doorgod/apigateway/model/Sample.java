package com.ymatou.doorgod.apigateway.model;

import com.ymatou.doorgod.apigateway.Utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 从收到的http请求{@link io.vertx.core.http.HttpServerRequest}中提取的样本
 * Created by tuwenjie on 2016/9/7.
 */
public class Sample {

    public static final String NULL_PLACEHOLDER = "NULL_FLAG";

    public static final String KEY_IP = "ip";

    public static final String KEY_URI = "uri";

    public static final String KEY_DEVICE_ID = "deviceId";

    //样本值
    private Map<String, String> dimensionValues = new TreeMap<String, String>( );


    public void addDimensionValue(String key, String value) {
        if ( key != null && key.trim().length() > 0 ) {
            if ( value != null ) {
                dimensionValues.put(key, value);
            } else {
                dimensionValues.put(key, NULL_PLACEHOLDER);
            }
        }
    }

    public void addIp(HttpServerRequest req ) {
        dimensionValues.put(KEY_IP, Utils.getOriginalIp(req));
    }

    public void addUri(HttpServerRequest req) {
        dimensionValues.put(KEY_URI, req.uri());
    }

    public void addDeviceId(HttpServerRequest req) {
        dimensionValues.put(KEY_DEVICE_ID, Utils.getDeviceId(req));
    }
}
