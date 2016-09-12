package com.ymatou.doorgod.apigateway.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 一般用于变更后，无需系统重启
 * Created by tuwenjie on 2016/9/5.
 */
@Component
@DisconfFile(fileName = "biz.properties")
public class BizConfig {


    private String targetWebServerHost;

    private int targetWebServerPort;

    private boolean enableHystrix;

    @DisconfFileItem(name = "targetWebServerHost")
    public String getTargetWebServerHost() {
        return targetWebServerHost;
    }

    public void setTargetWebServerHost(String targetWebServerHost) {
        this.targetWebServerHost = targetWebServerHost;
    }

    @DisconfFileItem(name = "targetWebServerPort")
    public int getTargetWebServerPort() {
        return targetWebServerPort;
    }

    public void setTargetWebServerPort(int targetWebServerPort) {
        this.targetWebServerPort = targetWebServerPort;
    }

    @DisconfFileItem(name = "enableHystrix")
    public boolean isEnableHystrix() {
        return enableHystrix;
    }

    public void setEnableHystrix(boolean enableHystrix) {
        this.enableHystrix = enableHystrix;
    }
}


