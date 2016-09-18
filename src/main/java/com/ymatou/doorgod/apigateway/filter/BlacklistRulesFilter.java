package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.cache.BlacklistRuleOffenderCache;
import com.ymatou.doorgod.apigateway.cache.RuleCache;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import io.vertx.core.http.HttpServerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 执行黑名单规则的Filter
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class BlacklistRulesFilter extends AbstractPreFilter {

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private BlacklistRuleOffenderCache offenderCache;

    @Override
    protected boolean passable(HttpServerRequest req, FilterContext context) {
        Set<BlacklistRule> rules = ruleCache.applicableBlacklistRules(req.uri());

        for ( BlacklistRule rule : rules ) {
            context.ruleName = rule.getName();
            Sample sample = context.sample.narrow(rule.getDimensionKeys());
            if ( offenderCache.isInBlacklist(rule.getName(), sample)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}