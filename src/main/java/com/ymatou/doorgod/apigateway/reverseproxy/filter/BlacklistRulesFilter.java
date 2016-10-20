package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.cache.RuleCache;
import com.ymatou.doorgod.apigateway.cache.RuleOffenderCache;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.Sample;
import io.vertx.core.http.HttpServerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 执行黑名单规则的Filter
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class BlacklistRulesFilter extends AbstractPreFilter {
    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private RuleOffenderCache ruleOffenderCache;

    @Override
    protected boolean passable(HttpServerRequest req, FilterContext context) {
        for (BlacklistRule rule : ruleCache.applicableBlacklistRules(context.hostUri)) {
            context.matchedRuleNames.add(rule.getName());
            if ( !rule.isObserverMode()) {
                Sample sample = context.sample.narrow( rule.getDimensionKeys());
                Date releaseDate = ruleOffenderCache.locateReleaseDate(rule.getName(), sample);
                if (releaseDate != null && releaseDate.compareTo(new Date()) >= 0) {
                    context.rejectRuleName = rule.getName();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
