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

    @Value("${targetWebServerHost}")
    private String targetWebServerHost;

    @Value("${targetWebServerPort}")
    private int targetWebServerPort;

    @Value("${enableHystrix}")
    private boolean enableHystrix;

    public int getVertxServerPort() {
        return vertxServerPort;
    }

    public void setVertxServerPort(int vertxServerPort) {
        this.vertxServerPort = vertxServerPort;
    }

    public String getTargetWebServerHost() {
        return targetWebServerHost;
    }

    public void setTargetWebServerHost(String targetWebServerHost) {
        this.targetWebServerHost = targetWebServerHost;
    }

    public int getTargetWebServerPort() {
        return targetWebServerPort;
    }

    public void setTargetWebServerPort(int targetWebServerPort) {
        this.targetWebServerPort = targetWebServerPort;
    }

    public boolean isEnableHystrix() {
        return enableHystrix;
    }

    public void setEnableHystrix(boolean enableHystrix) {
        this.enableHystrix = enableHystrix;
    }
}


