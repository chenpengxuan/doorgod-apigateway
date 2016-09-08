package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by tuwenjie on 2016/9/7.
 */
@Component
public class RuleCache implements Cache {
    private Set<LimitTimesRule> limitTimesRules = new TreeSet<LimitTimesRule>();

    private Set<BlacklistRule> blacklistRules = new TreeSet<BlacklistRule>();

    public Set<LimitTimesRule> getLimitTimesRules() {
        return limitTimesRules;
    }

    public Set<BlacklistRule> getBlacklistRules() {
        return blacklistRules;
    }

    @PostConstruct
    @Override
    public void reload() {

    }
}
