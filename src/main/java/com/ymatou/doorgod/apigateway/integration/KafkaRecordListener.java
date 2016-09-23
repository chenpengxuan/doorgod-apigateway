package com.ymatou.doorgod.apigateway.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Created by tuwenjie on 2016/9/18.
 */
public interface KafkaRecordListener {

    /**
     * 消费失败，抛异常
     * @param record
     * @throws Exception
     */
    void onRecordReceived( ConsumerRecord<String, String> record ) throws Exception ;
}
