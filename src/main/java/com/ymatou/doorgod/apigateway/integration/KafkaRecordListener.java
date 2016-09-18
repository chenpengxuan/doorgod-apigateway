package com.ymatou.doorgod.apigateway.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Created by tuwenjie on 2016/9/18.
 */
public interface KafkaRecordListener {

    boolean onRecordReceived( ConsumerRecord<String, String> record );
}
