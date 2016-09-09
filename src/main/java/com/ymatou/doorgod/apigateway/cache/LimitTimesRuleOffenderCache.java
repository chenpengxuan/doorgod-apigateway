package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.LimitTimesRuleOffender;
import com.ymatou.doorgod.apigateway.model.Sample;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class LimitTimesRuleOffenderCache implements Cache {

    /**
     * key: ruleName
     * value:
     *    key: sample
     *    value: sample + releaseTime
     */
    private Map<String, Map<Sample, LimitTimesRuleOffender>> offenders = new HashMap<>( );

    @PostConstruct
    @Override
    public void reload() {

    }

    public LimitTimesRuleOffender locate( String ruleName, Sample sample ) {
        Map<Sample, LimitTimesRuleOffender> offendersForRule = offenders.get(ruleName);
        return offendersForRule == null ? null : offendersForRule.get(sample);
    }
}
