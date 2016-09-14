package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.integration.RedisClient;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class BlacklistRuleOffenderCache implements Cache {

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private RedisClient redisClient;

    /**
     * key: ruleName
     * value: blacklist of the rule
     */
    private Map<String, Set<Sample>> offenders = new HashMap<>( );

    @PostConstruct
    @Override
    public void reload() throws Exception {
        for (BlacklistRule rule : ruleCache.getBlacklistRules()) {
            Set<Sample> samples = redisClient.getBlacklistRuleOffenders(rule.getName());
            offenders.put(rule.getName(), samples);
        }
    }

    public boolean isInBlacklist( String ruleName, Sample sample ) {
        Set<Sample> offendersForRule = offenders.get(ruleName);
        return offendersForRule == null ? false : offendersForRule.contains(sample);
    }
}
