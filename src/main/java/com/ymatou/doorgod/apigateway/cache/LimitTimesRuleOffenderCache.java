package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.integration.RedisClient;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import com.ymatou.doorgod.apigateway.model.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class LimitTimesRuleOffenderCache implements Cache {

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private RedisClient redisClient;

    /**
     * key: ruleName
     * value:
     *    key: sample
     *    value: releaseDate: 释放时间（即被允许下次访问的时间）
     */
    private Map<String, Map<Sample, Date>> offenders = new HashMap<>( );

    @PostConstruct
    @Override
    public void reload() throws Exception {
        for (LimitTimesRule rule : ruleCache.getLimitTimesRules()) {
            Map<Sample, Date> result = redisClient.getLimitTimesRuleOffenders(rule.getName());
            offenders.put(rule.getName(), result);
        }
    }

    public Date locateReleaseDate( String ruleName, Sample sample ) {
        Map<Sample, Date> offendersForRule = offenders.get(ruleName);
        return offendersForRule == null ? null : offendersForRule.get(ruleName);
    }
}
