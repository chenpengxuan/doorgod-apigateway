package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.KeyAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class KeyAliasCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    /**
     * Key:uri
     * value
     *      key: originalKeyName
     *      value: alias
     */
    private Map<String, Map<String, String>> aliases = new HashMap<>( );

    @Override
    public void reload() throws Exception {
        List<KeyAlias> loadeds =  mySqlClient.loadKeyAliases();
        Map<String, Map<String, String>> result = new HashMap<>( );

        for (KeyAlias alias : loadeds ) {
            if ( result.get(alias.getUri()) == null ) {
                result.put(alias.getUri(), new HashMap<String, String>( ));
            }
            result.get(alias.getUri()).put(alias.getKey(), alias.getAlias());
        }

        aliases = result;
    }

    public String getAlias( String uri, String key ) {
        Map<String, String> value = aliases.get(uri);
        return value == null ? null : value.get(key);
    }

}
