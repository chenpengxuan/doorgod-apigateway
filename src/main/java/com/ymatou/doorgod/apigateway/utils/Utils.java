package com.ymatou.doorgod.apigateway.utils;

import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static volatile String localIp;

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
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static Date parseDate( String date ) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String localIp( ) {
        if (localIp != null) {
            return localIp;
        }
        synchronized (Utils.class) {
            if (localIp == null) {
                try {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                            .getNetworkInterfaces();

                    while (netInterfaces.hasMoreElements() && localIp == null) {
                        NetworkInterface ni = netInterfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp() && !ni.isVirtual()) {
                            Enumeration<InetAddress> address = ni.getInetAddresses();

                            while (address.hasMoreElements() && localIp == null) {
                                InetAddress addr = address.nextElement();

                                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()
                                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                                    localIp = addr.getHostAddress();

                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    localIp = "127.0.0.1";
                    LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead", t);
                }
            }

            if (localIp == null ) {
                localIp = "127.0.0.1";
                LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead");
            }

            return localIp;
        }
    }
}
