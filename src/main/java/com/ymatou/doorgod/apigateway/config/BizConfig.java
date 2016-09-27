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

}


