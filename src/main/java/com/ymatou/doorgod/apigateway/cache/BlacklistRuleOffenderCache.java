package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.LimitTimesRuleOffender;
import com.ymatou.doorgod.apigateway.model.Sample;
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

    /**
     * key: ruleName
     * value: blacklist of the rule
     */
    private Map<String, Set<Sample>> offenders = new HashMap<>( );

    @PostConstruct
    @Override
    public void reload() {

    }

    public boolean isInBlacklist( String ruleName, Sample sample ) {
        Set<Sample> offendersForRule = offenders.get(ruleName);
        return offendersForRule == null ? false : offendersForRule.contains(sample);
    }
}
