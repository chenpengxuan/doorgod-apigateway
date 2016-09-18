package com.ymatou.doorgod.apigateway.integration;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.RejectReqEvent;
import com.ymatou.doorgod.apigateway.model.StatisticItem;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by tuwenjie on 2016/9/9.
 */
@Component
public class KafkaClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(KafkaClient.class);

    private Producer<String, String> producer;

    private Consumer<String, String> consumer;

    @Autowired
    private AppConfig appConfig;

    private String localIp;

    @Autowired
    private KafkaRecordListener kafkaRecordListener;



    @PostConstruct
    public void init (){
        localIp = Utils.localIp();
        if ( localIp == null || localIp.equals("127.0.0.1")) {
            //本地ip将用作groupId, 本地Ip拿不到，拒绝应用启动
            throw new RuntimeException("Failed to fetch local ip");
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", appConfig.getKafkaUrl());
        props.put("acks", "0");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);


        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", appConfig.getKafkaUrl());
        consumerProps.put("group.id", localIp);
        consumerProps.put("client.id", localIp);
        consumerProps.put("enable.auto.commit", "false");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(consumerProps);

        consumer.subscribe(Arrays.asList(Constants.TOPIC_OFFENDERS_UPDATE_EVENT));

        Thread thread = new Thread(()->{
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);

                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);

                    //收到一个Partition的多个record，只处理最一个record。避免重复刷新同一缓存
                    kafkaRecordListener.onRecordReceived( partitionRecords.get(partitionRecords.size() - 1));
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    consumer.commitAsync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)),
                            (offsets, exception) -> {
                                LOGGER.error("Failed to commit kafaka offsets", exception);
                            });
                }
            }
        } );
        thread.setDaemon(true);
        thread.setName("kafka-consumer-thread");

        thread.start();

    }


    @PreDestroy
    public void destroy( ) {
        producer.close();
        consumer.wakeup();
        consumer.close();
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
