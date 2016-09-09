package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by tuwenjie on 2016/9/7.
 */
@Component
public class RuleCache implements Cache {
    private Set<LimitTimesRule> limitTimesRules = new TreeSet<LimitTimesRule>();

    private Set<BlacklistRule> blacklistRules = new TreeSet<BlacklistRule>();

    private Set<String> allDimensionKeys = new HashSet<String>( );



    @PostConstruct
    @Override
    public void reload() {
        fillDimensionKeys();
    }

    public Set<LimitTimesRule> applicableLimitTimesRules( String uri ) {
        return limitTimesRules.stream().filter(limitTimesRule -> limitTimesRule.applicable(uri))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<BlacklistRule> applicableBlacklistRules( String uri ) {
        return blacklistRules.stream().filter(blacklistRule -> blacklistRule.applicable(uri))
                .collect(Collectors.toCollection(TreeSet::new));
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
}
