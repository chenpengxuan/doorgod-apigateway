package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.AbstractRule;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tuwenjie on 2016/9/7.
 */
@Component
public class RuleCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;


    private Set<BlacklistRule> blacklistRules = new TreeSet<BlacklistRule>();

    private Set<LimitTimesRule> limitTimesRules = new TreeSet<LimitTimesRule>();

    //ruleName -> rule
    private Map<String, AbstractRule> nameToRules = new HashMap<String, AbstractRule>( );


    private Set<String> allDimensionKeys = new HashSet<String>( );

    //基于uri的缓存, key: uri
    private LoadingCache<String, Set<BlacklistRule>> uriToBlacklistRulesCache;
    private LoadingCache<String, Set<LimitTimesRule>> uriToLimitTimesRulesCache;

    @PostConstruct
    @Override
    public void reload() throws Exception {
        Set[] result = mySqlClient.loadAllRules();
        blacklistRules = result[0];
        limitTimesRules = result[1];
        fillDimensionKeys();
        buildNameToRules( );

        if (uriToBlacklistRulesCache == null) {
            uriToBlacklistRulesCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<String, Set<BlacklistRule>>() {
                                public Set<BlacklistRule> load(String uri) {
                                    return blacklistRules.stream().filter(blacklistRule -> blacklistRule.applicable(uri))
                                            .collect(Collectors.toCollection(TreeSet::new));
                                }
                            });
        }

        if (uriToLimitTimesRulesCache == null) {
            uriToLimitTimesRulesCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<String, Set<LimitTimesRule>>() {
                                public Set<LimitTimesRule> load(String uri) {
                                    return limitTimesRules.stream().filter(blacklistRule -> blacklistRule.applicable(uri))
                                            .collect(Collectors.toCollection(TreeSet::new));
                                }
                            });
        }
        uriToBlacklistRulesCache.invalidateAll();
        uriToLimitTimesRulesCache.invalidateAll();
    }

    public Set<LimitTimesRule> applicableLimitTimesRules( String uri ) {
        return uriToLimitTimesRulesCache.getUnchecked(uri);
    }

    public Set<BlacklistRule> applicableBlacklistRules( String uri ) {
        return uriToBlacklistRulesCache.getUnchecked(uri);
    }

    private Set<String> fillDimensionKeys( ) {
        Set<String> result = limitTimesRules.stream().map(limitTimesRule -> limitTimesRule.getDimensionKeys())
                .flatMap(strings -> strings.stream()).collect(Collectors.toSet());
        result.addAll(
                blacklistRules.stream().map(blacklistRules -> blacklistRules.getDimensionKeys())
                        .flatMap(strings -> strings.stream()).collect(Collectors.toSet())
        );
        return result;
    }

    public Set<String> getAllDimensionKeys() {
        return allDimensionKeys;
    }

    public Set<BlacklistRule> getBlacklistRules() {
        return blacklistRules;
    }

    public Set<LimitTimesRule> getLimitTimesRules() {
        return limitTimesRules;
    }

    private void buildNameToRules( ) {
        blacklistRules.stream().forEach(rule -> nameToRules.put(rule.getName(), rule));
        limitTimesRules.stream().forEach(rule -> nameToRules.put(rule.getName(), rule));
    }

    public AbstractRule locate( String ruleName ) {
        return nameToRules.get(ruleName);
    }
}
