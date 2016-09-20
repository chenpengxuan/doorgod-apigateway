package com.ymatou.doorgod.apigateway.cache;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.integration.KafkaRecordListener;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class CacheLoader implements KafkaRecordListener {

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private LimitTimesRuleOffenderCache limitTimesRuleOffenderCache;

    @Autowired
    private BlacklistRuleOffenderCache blacklistRuleOffenderCache;

    @Override
    public boolean onRecordReceived(ConsumerRecord<String, String> record) throws Exception {
        switch (record.topic()) {
            case Constants.TOPIC_OFFENDERS_UPDATE_EVENT:
                UpdateOffendersEvent event = JSON.parseObject(record.value(), UpdateOffendersEvent.class);
                if ( ruleCache.locate(event.getRuleName()) instanceof LimitTimesRule ) {
                    limitTimesRuleOffenderCache.reload(event.getRuleName());
                } else if (ruleCache.locate(event.getRuleName()) instanceof BlacklistRule ) {
                    blacklistRuleOffenderCache.reload(event.getRuleName());
                }
                break;

            default:
        }

        return true;
    }
}
