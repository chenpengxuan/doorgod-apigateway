package com.ymatou.doorgod.apigateway.Utils;

import com.ymatou.doorgod.apigateway.model.Sample;
import io.vertx.core.http.HttpServerRequest;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class Utils {

    public static String EMPTY_DEVICE_ID_PLACEHOLDER = "EMPTY_FLAG";

    private Utils() {};

    /**
     * 获取来源IP
     * @return
     */
    public static String getOriginalIp(HttpServerRequest req ) {
        String result = (req.headers().get("X-Forwarded-For"));
        if ( result == null || result.trim().length() == 0) {
            return req.remoteAddress().host();
        } else {
            return result;
        }
    }

    public static String getDeviceId( HttpServerRequest req)  {
        //TODO:
        return Sample.NULL_PLACEHOLDER;
    }
}
