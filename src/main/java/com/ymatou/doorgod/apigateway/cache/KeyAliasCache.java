package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.*;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.KeyAlias;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class KeyAliasCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private Set<KeyAlias> aliases = new HashSet<KeyAlias>( );

    /**
     * Key:uri
     * value
     *      key: originalKeyName
     *      value: alias
     */
    private LoadingCache<String, Map<String, String>> uriToAliases;

    @PostConstruct
    @Override
    public void reload() throws Exception {
        aliases =  mySqlClient.loadKeyAliases();
        if (uriToAliases == null) {
            uriToAliases = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new com.google.common.cache.CacheLoader<String, Map<String, String>>() {
                                public Map<String, String> load(String uri) {
                                    Map<String, String> result = new HashMap<String, String>( );

                                    //uri长的优先匹配
                                    aliases.stream().sorted().forEach(keyAlias -> {
                                        if ( keyAlias.applicable(uri)) {
                                            if ( !result.containsKey(keyAlias.getKey())) {
                                                result.put(keyAlias.getKey(), keyAlias.getAlias());
                                            }
                                        }
                                    });
                                    return result;
                                }
                            });
        }

        uriToAliases.invalidateAll();
    }

    public String getAlias( String uri, String key ) {
        return uriToAliases.getUnchecked(uri).get(key);
    }

}
