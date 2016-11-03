package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class UriPatternCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private List<String> patterns = new ArrayList<String>( );

    /**
     * Key:uri
     * value: pattern
     */
    private LoadingCache<String, Optional<String>> uriToPatterns;

    @PostConstruct
    @Override
    public void reload() throws Exception {
        List<String> loadeds  =  mySqlClient.loadUriPatterns();

        //长的优先匹配
        Collections.sort(loadeds, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        patterns = loadeds;

        if (uriToPatterns == null) {
            uriToPatterns = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new com.google.common.cache.CacheLoader<String, Optional<String>>() {
                                public Optional<String> load(String uri) {
                                    for ( String pattern : patterns) {
                                        try {
                                            if (Pattern.matches(pattern, uri)) {
                                                return Optional.of(pattern);
                                            }
                                        } catch (Exception e) {
                                            LOGGER.error("Failed to parse pattern:{} in uri pattern. {}", pattern, e.getMessage(), e);
                                        }
                                    }
                                    return Optional.empty();
                                }
                            });
        }

        uriToPatterns.invalidateAll();
    }

    public String getPattern( String uri ) {
        return uriToPatterns.getUnchecked(uri).orElse(null);
    }

}
