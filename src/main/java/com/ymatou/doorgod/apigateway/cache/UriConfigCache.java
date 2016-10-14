package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import com.ymatou.doorgod.apigateway.model.UriConfig;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class UriConfigCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private List<UriConfig> configs = new ArrayList<UriConfig>();

    private LoadingCache<String, UriConfig> uriToConfigCache;

    @PostConstruct
    @Override
    public void reload() throws Exception {

        List<UriConfig> configs = mySqlClient.loadUriConfigs();

        //uri长的在前面
        Collections.sort(configs);

        this.configs = configs;

        if (uriToConfigCache == null) {
            uriToConfigCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<String, UriConfig>() {
                                public UriConfig load(String uri) {
                                    for ( UriConfig config : configs ) {
                                        if ( uri.toLowerCase().startsWith(config.getUri())) {
                                            return config;
                                        }
                                    }
                                    return null;
                                }
                            });
        }

        uriToConfigCache.invalidateAll();

    }



    public UriConfig locate( String uri ) {
        return uriToConfigCache.getUnchecked(uri);
    }
}
