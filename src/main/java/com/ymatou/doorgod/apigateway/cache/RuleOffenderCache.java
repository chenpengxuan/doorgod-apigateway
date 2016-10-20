package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.integration.MongodbClient;
import com.ymatou.doorgod.apigateway.model.*;
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
public class RuleOffenderCache implements Cache {

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private MongodbClient mongodbClient;

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
            Map<Sample, Date> result = mongodbClient.loadRuleOffenders(rule.getName(), RuleTypeEnum.LimitTimesRule);
            offenders.put(rule.getName(), result);
        }
        for (BlacklistRule rule : ruleCache.getBlacklistRules()) {
            Map<Sample, Date> result = mongodbClient.loadRuleOffenders(rule.getName(), RuleTypeEnum.BlacklistRule);
            offenders.put(rule.getName(), result);
        }
    }

    public Date locateReleaseDate( String ruleName, Sample sample ) {
        Map<Sample, Date> offendersForRule = offenders.get(ruleName);
        return offendersForRule == null ? null : offendersForRule.get(sample);
    }

    public void reload( String ruleName ) throws Exception {
        AbstractRule rule = ruleCache.locate(ruleName);
        Map<Sample, Date> result = mongodbClient.loadRuleOffenders(ruleName, rule.type());
        offenders.put(ruleName, result);
    }
}
