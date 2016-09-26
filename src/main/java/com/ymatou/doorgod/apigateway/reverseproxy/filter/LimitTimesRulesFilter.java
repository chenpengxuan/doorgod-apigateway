package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.cache.LimitTimesRuleOffenderCache;
import com.ymatou.doorgod.apigateway.cache.RuleCache;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import com.ymatou.doorgod.apigateway.model.Sample;
import io.vertx.core.http.HttpServerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class LimitTimesRulesFilter extends AbstractPreFilter {

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private LimitTimesRuleOffenderCache offenderCache;

    @Override
    protected boolean passable(HttpServerRequest req, FilterContext context) {
        for (LimitTimesRule rule : ruleCache.applicableLimitTimesRules(req.path())) {
            context.ruleName = rule.getName();
            Sample sample = context.sample.narrow(
                    CollectionUtils.isEmpty(rule.getGroupByKeys()) ? rule.getDimensionKeys() : rule.getGroupByKeys());
            Date releaseDate = offenderCache.locateReleaseDate(rule.getName(), sample);
            if ( releaseDate != null && releaseDate.compareTo(new Date( )) >= 0 ) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
