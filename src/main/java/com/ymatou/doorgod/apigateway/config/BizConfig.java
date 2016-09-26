package com.ymatou.doorgod.apigateway.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Component;

/**
 * 一般用于变更后，无需系统重启的配置
 * Created by tuwenjie on 2016/9/5.
 */
@Component
@DisconfFile(fileName = "biz.properties")
public class BizConfig {


    private String targetWebServerHost;

    private int targetWebServerPort;


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

}


