package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class HystrixConfigCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private Map<String, HystrixConfig> urlToConfigs = new HashMap<String, HystrixConfig>();


    @Override
    public void reload() throws Exception {

        List<HystrixConfig> configs = mySqlClient.loadHystrixConfigs();

        Map<String, HystrixConfig> urlToConfigs = new HashMap<String, HystrixConfig>();

        for ( HystrixConfig config : configs ) {
            urlToConfigs.put(config.getUri(), config);
        }

        this.urlToConfigs = urlToConfigs;

    }

    public HystrixConfig locate( String uri ) {
        return urlToConfigs.get(uri);
    }
}
