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

    @Value("${kafkaUrl}")
    private String kafkaUrl;

    @Value("${maxHttpConnectionPoolSize}")
    private int maxHttpConnectionPoolSize;

    @Value("${targetServerWarmupUri}")
    private String targetServerWarmupUri;

    @Value("${connectionIdleTimeout}")
    private int connectionIdleTimeout;

    @Value("${acceptBacklog}")
    private int acceptBacklog;

    @Value("${mySqlHost}")
    private String mySqlHost;

    @Value("${mySqlPort}")
    private int mySqlPort;

    @Value("${mySqlUser}")
    private String mySqlUser;

    @Value("${mySqlPassword}")
    private String mySqlPassword;

    @Value("${mySqlDbName}")
    private String mySqlDbName;

    @Value("${mongodbName}")
    private String mongodbName;

    @Value("${mongodbUrl}")
    private String mongodbUrl;

    @Value("${debugMode}")
    private boolean debugMode;

    @Value("${maxUriLength}")
    private int maxUriLength;

    @Value("${kafkaSendTimeout}")
    private int kafkaSendTimeout;

    @Value("${performanceServerUrl}")
    private String performanceServerUrl;

    public int getVertxServerPort() {
        return vertxServerPort;
    }

    public void setVertxServerPort(int vertxServerPort) {
        this.vertxServerPort = vertxServerPort;
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

    public String getTargetServerWarmupUri() {
        return targetServerWarmupUri;
    }

    public void setTargetServerWarmupUri(String targetServerWarmupUri) {
        this.targetServerWarmupUri = targetServerWarmupUri;
    }

    public String getMySqlHost() {
        return mySqlHost;
    }

    public void setMySqlHost(String mySqlHost) {
        this.mySqlHost = mySqlHost;
    }

    public int getMySqlPort() {
        return mySqlPort;
    }

    public void setMySqlPort(int mySqlPort) {
        this.mySqlPort = mySqlPort;
    }

    public String getMySqlUser() {
        return mySqlUser;
    }

    public void setMySqlUser(String mySqlUser) {
        this.mySqlUser = mySqlUser;
    }

    public String getMySqlPassword() {
        return mySqlPassword;
    }

    public void setMySqlPassword(String mySqlPassword) {
        this.mySqlPassword = mySqlPassword;
    }

    public String getMySqlDbName() {
        return mySqlDbName;
    }

    public void setMySqlDbName(String mySqlDbName) {
        this.mySqlDbName = mySqlDbName;
    }

    public String getMongodbName() {
        return mongodbName;
    }

    public void setMongodbName(String mongodbName) {
        this.mongodbName = mongodbName;
    }

    public String getMongodbUrl() {
        return mongodbUrl;
    }

    public void setMongodbUrl(String mongodbUrl) {
        this.mongodbUrl = mongodbUrl;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getMaxUriLength() {
        return maxUriLength;
    }

    public void setMaxUriLength(int maxUriLength) {
        this.maxUriLength = maxUriLength;
    }

    public int getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    public void setConnectionIdleTimeout(int connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    public int getAcceptBacklog() {
        return acceptBacklog;
    }

    public void setAcceptBacklog(int acceptBacklog) {
        this.acceptBacklog = acceptBacklog;
    }

    public int getKafkaSendTimeout() {
        return kafkaSendTimeout;
    }

    public void setKafkaSendTimeout(int kafkaSendTimeout) {
        this.kafkaSendTimeout = kafkaSendTimeout;
    }

    public String getPerformanceServerUrl() {
        return performanceServerUrl;
    }

    public void setPerformanceServerUrl(String performanceServerUrl) {
        this.performanceServerUrl = performanceServerUrl;
    }
}


