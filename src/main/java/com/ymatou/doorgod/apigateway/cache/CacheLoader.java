package com.ymatou.doorgod.apigateway.cache;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.integration.KafkaRecordListener;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 *
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class CacheLoader implements KafkaRecordListener {

    @Override
    public boolean onRecordReceived(ConsumerRecord<String, String> record) {
        switch (record.topic()) {
            case Constants.TOPIC_OFFENDERS_UPDATE_EVENT:
                UpdateOffendersEvent event = JSON.parseObject(record.value(), UpdateOffendersEvent.class);

            default:
        }

        return true;
    }
}
