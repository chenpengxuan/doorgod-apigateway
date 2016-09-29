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

    @Value("${initHttpConnections}")
    private int initHttpConnections;

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

    @Value("${enableHystrix}")
    private boolean enableHystrix;

    @Value("${mongodbName}")
    private String mongodbName;

    @Value("${mongodbUrl}")
    private String mongodbUrl;

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

    public int getInitHttpConnections() {
        return initHttpConnections;
    }

    public void setInitHttpConnections(int initHttpConnections) {
        this.initHttpConnections = initHttpConnections;
    }

    public boolean isEnableHystrix() {
        return enableHystrix;
    }

    public void setEnableHystrix(boolean enableHystrix) {
        this.enableHystrix = enableHystrix;
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
}


