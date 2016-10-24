package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.AbstractRule;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.HostUri;
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


    private List<BlacklistRule> blacklistRules = new ArrayList<BlacklistRule>();

    private List<LimitTimesRule> limitTimesRules = new ArrayList<LimitTimesRule>();

    //ruleName -> rule
    private Map<String, AbstractRule> nameToRules = new HashMap<String, AbstractRule>( );


    private Set<String> allDimensionKeys = new HashSet<String>( );

    //基于HostUri的缓存
    private LoadingCache<HostUri, List<BlacklistRule>> uriToBlacklistRulesCache;
    private LoadingCache<HostUri, List<LimitTimesRule>> uriToLimitTimesRulesCache;

    @PostConstruct
    @Override
    public void reload() throws Exception {

        Set<AbstractRule> result = mySqlClient.loadAllRules();

        List<BlacklistRule> reloadedBlacklistRules = new ArrayList<BlacklistRule>();
        List<LimitTimesRule> reloadedLimitTimesRules = new ArrayList<LimitTimesRule>();

        result.stream().filter(rule-> rule instanceof BlacklistRule).sorted().forEach(
                rule -> reloadedBlacklistRules.add((BlacklistRule)rule));
        result.stream().filter(rule-> rule instanceof LimitTimesRule).sorted().forEach(
                rule -> reloadedLimitTimesRules.add((LimitTimesRule)rule));

        this.blacklistRules = reloadedBlacklistRules;
        this.limitTimesRules = reloadedLimitTimesRules;

        fillDimensionKeys();

        buildNameToRules( );

        if (uriToBlacklistRulesCache == null) {
            uriToBlacklistRulesCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<HostUri, List<BlacklistRule>>() {
                                public List<BlacklistRule> load(HostUri hostUri) {
                                    return blacklistRules.stream().filter(rule -> rule.applicable(hostUri))
                                            .collect(Collectors.toList());
                                }
                            });
        }

        if (uriToLimitTimesRulesCache == null) {
            uriToLimitTimesRulesCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<HostUri, List<LimitTimesRule>>() {
                                public List<LimitTimesRule> load(HostUri hostUri) {
                                    return limitTimesRules.stream().filter(rule -> rule.applicable(hostUri))
                                            .collect(Collectors.toList());
                                }
                            });
        }
        uriToBlacklistRulesCache.invalidateAll();
        uriToLimitTimesRulesCache.invalidateAll();
    }

    public List<LimitTimesRule> applicableLimitTimesRules( HostUri hostUri ) {
        return uriToLimitTimesRulesCache.getUnchecked(hostUri);
    }

    public List<BlacklistRule> applicableBlacklistRules( HostUri hostUri ) {
        return uriToBlacklistRulesCache.getUnchecked(hostUri);
    }

    private void fillDimensionKeys( ) {
        Set<String> result = limitTimesRules.stream().map(limitTimesRule -> limitTimesRule.getDimensionKeys())
                .flatMap(strings -> strings.stream()).collect(Collectors.toSet());
        result.addAll(
                blacklistRules.stream().map(blacklistRules -> blacklistRules.getDimensionKeys())
                        .flatMap(strings -> strings.stream()).collect(Collectors.toSet())
        );
        allDimensionKeys = result;
    }

    public Set<String> getAllDimensionKeys() {
        return allDimensionKeys;
    }

    public List<BlacklistRule> getBlacklistRules() {
        return blacklistRules;
    }

    public List<LimitTimesRule> getLimitTimesRules() {
        return limitTimesRules;
    }

    private void buildNameToRules( ) {
        Map<String, AbstractRule> nameToRules = new HashMap<String, AbstractRule>( );
        blacklistRules.stream().forEach(rule -> nameToRules.put(rule.getName(), rule));
        limitTimesRules.stream().forEach(rule -> nameToRules.put(rule.getName(), rule));

        this.nameToRules = nameToRules;
    }

    public AbstractRule locate( String ruleName ) {
        return nameToRules.get(ruleName);
    }
}
