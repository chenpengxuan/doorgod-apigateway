package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.UriConfig;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class UriConfigCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private List<UriConfig> configs = new ArrayList<UriConfig>();

    private LoadingCache<String, Optional<UriConfig>> uriToConfigCache;

    @PostConstruct
    @Override
    public void reload() throws Exception {

        List<UriConfig> reloaded = mySqlClient.loadUriConfigs();

        //uri长的在前面
        Collections.sort(reloaded);

        this.configs = reloaded;

        if (uriToConfigCache == null) {
            uriToConfigCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<String, Optional<UriConfig>>() {
                                public Optional<UriConfig> load(String uri) {
                                    for ( UriConfig config : configs ) {
                                        try {
                                            if (uri.toLowerCase().startsWith(config.getUri())
                                                    || Pattern.matches(config.getUri(), uri.toLowerCase())) {
                                                return Optional.of(config);
                                            }
                                        } catch ( Exception e ) {
                                            LOGGER.error("Failed to parse Pattern:{} in uri config. {}", config.getUri(), e.getMessage(), e);
                                        }
                                    }
                                    return Optional.empty();
                                }
                            });
        }

        uriToConfigCache.invalidateAll();

    }



    public UriConfig locate( String uri ) {
        return uriToConfigCache.getUnchecked(uri).orElse(null);
    }
}
