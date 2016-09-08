package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.cache.RuleCache;
import io.vertx.core.http.HttpServerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 执行黑名单规则的Filter
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class BlacklistRulesFilter implements  PreFilter {

    @Autowired
    private RuleCache ruleCache;

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean pass(HttpServerRequest req) {
        ruleCache.getBlacklistRules();
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
