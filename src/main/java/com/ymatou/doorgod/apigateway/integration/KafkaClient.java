package com.ymatou.doorgod.apigateway.integration;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.RejectReqEvent;
import com.ymatou.doorgod.apigateway.model.StatisticItem;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

/**
 * Created by tuwenjie on 2016/9/9.
 */
@Component
public class KafkaClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(KafkaClient.class);

    private Producer<String, String> producer;

    @Autowired
    private AppConfig appConfig;



    @PostConstruct
    public void init (){
        Properties props = new Properties();
        props.put("bootstrap.servers", appConfig.getKafkaUrl());
        props.put("acks", "0");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
    }


    @PreDestroy
    public void destroy( ) {
        producer.close();
    }

    public void sendStatisticItem(StatisticItem item ) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(Constants.TOPIC_STATISTIC_SAMPLE_EVENT, JSON.toJSONString(item));
        producer.send(record, (metadata, exception) -> {
            if (exception != null ) {
                LOGGER.error("Failed to send statistic item to Kafka", exception);
            }
        });
    }

    public void sendRejectReqEvent(RejectReqEvent event ) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(Constants.TOPIC_REJECT_REQ_EVENT, JSON.toJSONString(event));
        producer.send(record, (metadata, exception) -> {
            if (exception != null ) {
                LOGGER.error("Failed to send reject req event to Kafka", exception);
            }
        });
    }
}
