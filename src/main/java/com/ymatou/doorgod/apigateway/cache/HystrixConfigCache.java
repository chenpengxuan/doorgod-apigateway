package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class HystrixConfigCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private Map<String, HystrixConfig> uriToConfigs = new HashMap<String, HystrixConfig>();

    @PostConstruct
    @Override
    public void reload() throws Exception {

        List<HystrixConfig> configs = mySqlClient.loadHystrixConfigs();

        Map<String, HystrixConfig> uriToConfigs = new HashMap<String, HystrixConfig>();

        for ( HystrixConfig config : configs ) {
            uriToConfigs.put(config.getUri(), config);
        }

        this.uriToConfigs = uriToConfigs;

    }

    public HystrixConfig locate( String uri ) {
        return uriToConfigs.get(uri);
    }
}
