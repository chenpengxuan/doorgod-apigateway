package com.ymatou.doorgod.apigateway.utils;

import io.vertx.core.http.HttpServerRequest;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class Utils {

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

    public static Set<String> splitByComma( String text ) {
        Set<String> result = new HashSet<String>( );
        if (StringUtils.hasText(text)) {
            text = text.trim();
            String[] splits = text.split(",");
            for (String split : splits ) {
                if (StringUtils.hasText(split.trim())) {
                    result.add(split.trim());
                }
            }
        }
        return result;
    }

    public static String getCurrentTime( ) {
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
    }
}
