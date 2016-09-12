package com.ymatou.doorgod.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 一般用于变更后，需要系统重启才能生效的配置
 * Created by tuwenjie on 2016/9/5.
 */
@Component
public class AppConfig {

    @Value("${vertxServerPort}")
    private int vertxServerPort;

    @Value("${redisUrl}")
    private String redisUrl;

    @Value("${kafkaUrl}")
    private String kafkaUrl;

    @Value("${maxHttpConnectionPoolSize}")
    private int maxHttpConnectionPoolSize;

    public int getVertxServerPort() {
        return vertxServerPort;
    }

    public void setVertxServerPort(int vertxServerPort) {
        this.vertxServerPort = vertxServerPort;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public void setRedisUrl(String redisUrl) {
        this.redisUrl = redisUrl;
    }

    public String getKafkaUrl() {
        return kafkaUrl;
    }

    public void setKafkaUrl(String kafkaUrl) {
        this.kafkaUrl = kafkaUrl;
    }

    public int getMaxHttpConnectionPoolSize() {
        return maxHttpConnectionPoolSize;
    }

    public void setMaxHttpConnectionPoolSize(int maxHttpConnectionPoolSize) {
        this.maxHttpConnectionPoolSize = maxHttpConnectionPoolSize;
    }
}


