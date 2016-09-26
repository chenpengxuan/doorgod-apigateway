package com.ymatou.doorgod.apigateway.cache;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.integration.KafkaRecordListener;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import com.ymatou.doorgod.apigateway.model.KeyAlias;
import com.ymatou.doorgod.apigateway.model.LimitTimesRule;
import com.ymatou.doorgod.apigateway.reverseproxy.hystrix.DynamicHystrixPropertiesStrategy;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class CacheReloader implements KafkaRecordListener {

    private static Logger LOGGER = LoggerFactory.getLogger(CacheReloader.class);

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private LimitTimesRuleOffenderCache limitTimesRuleOffenderCache;

    @Autowired
    private BlacklistRuleOffenderCache blacklistRuleOffenderCache;

    @Autowired
    private HystrixConfigCache hystrixConfigCache;

    @Autowired
    private KeyAliasCache keyAliasCache;

    @Autowired
    private DynamicHystrixPropertiesStrategy dynamicHystrixPropertiesStrategy;


    @Override
    public void onRecordReceived(ConsumerRecord<String, String> record) throws Exception {

        try {
            switch (record.topic()) {
                case Constants.TOPIC_UPDATE_OFFENDER_EVENT:
                    UpdateOffenderEvent event = JSON.parseObject(record.value(), UpdateOffenderEvent.class);
                    reloadOffenderCache(event.getRuleName());
                    break;
                case Constants.TOPIC_UPDATE_RULE_EVENT:
                    ruleCache.reload();
                    break;
                case Constants.TOPIC_UPDATE_KEY_ALIAS_EVENT:
                    keyAliasCache.reload();
                    break;
                case Constants.TOPIC_UPDATE_HYSTRIX_CONFIG_EVENT:
                    hystrixConfigCache.reload();
                    dynamicHystrixPropertiesStrategy.reload( );
                    break;
                default:
                    LOGGER.error("Receive unknow kafka message:{}", record);
            }
        } catch ( Exception e ) {
            //记录下具体消费那条日志失败
            LOGGER.error("Failed to consume kafka message:{}", record);
            throw e;
        }
    }

    private void reloadOffenderCache(  String ruleName ) throws Exception {
        if ( ruleCache.locate(ruleName) instanceof LimitTimesRule ) {
            limitTimesRuleOffenderCache.reload(ruleName);
        } else if (ruleCache.locate(ruleName) instanceof BlacklistRule ) {
            blacklistRuleOffenderCache.reload(ruleName);
        } else {
            LOGGER.error("Unknown rule name in UpdateOffenderEvent from kafka:{}", ruleName);
        }
    }
}
