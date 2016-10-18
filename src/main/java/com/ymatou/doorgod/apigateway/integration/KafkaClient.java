package com.ymatou.doorgod.apigateway.integration;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.StatisticItem;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuwenjie on 2016/9/9.
 */
@Component
public class KafkaClient {


    public static final Logger LOGGER = LoggerFactory.getLogger(KafkaClient.class);

    private Producer<String, String> producer;

    private Consumer<String, String> consumer;

    //单线程发送Kafka消息
    private ExecutorService producerExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(10000));

    @Autowired
    private AppConfig appConfig;

    private String localIp;

    @Autowired
    private KafkaRecordListener kafkaRecordListener;

    @PostConstruct
    public void init() {
        localIp = Utils.localIp();
        if (localIp == null || localIp.equals("127.0.0.1")) {
            //本地ip将用作groupId, 本地Ip拿不到，拒绝应用启动
            throw new RuntimeException("Failed to fetch local ip");
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", appConfig.getKafkaUrl());

        //无需关心消息是否真正送达
        props.put("acks", "0");
        props.put("client.id", localIp);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);


        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", appConfig.getKafkaUrl());
        consumerProps.put("group.id", localIp);
        consumerProps.put("client.id", localIp);

        //手动确认消息是否消费成功，确保没有消息被漏消费
        consumerProps.put("enable.auto.commit", "false");

        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(consumerProps);

        consumer.subscribe(Arrays.asList(Constants.TOPIC_UPDATE_OFFENDER_EVENT,
                Constants.TOPIC_UPDATE_RULE_EVENT,
                Constants.TOPIC_UPDATE_HYSTRIX_CONFIG_EVENT,
                Constants.TOPIC_UPDATE_KEY_ALIAS_EVENT));

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(5000);

                    for (TopicPartition partition : records.partitions()) {

                        List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);

                        try {
                            for (ConsumerRecord<String, String> record : partitionRecords) {
                                //都是缓存刷新消息，不频繁。日志输出，便于问题确认/分析
                                LOGGER.info("Recv kafka message:{}", record);
                                kafkaRecordListener.onRecordReceived(record);
                            }

                            /**
                             * 手动向Kafka确认那些消息已被成功消费。确保没有消息被漏消费。
                             * 存在消息被重复消费的case。例如<code>kafkaRecordListener.onRecordReceived()</code>消费失败，抛异常。
                             */
                            consumer.commitAsync(Collections.singletonMap(partition, new OffsetAndMetadata(
                                            partitionRecords.get(partitionRecords.size() - 1).offset() + 1)),
                                    (offsets, exception) -> {
                                        if (exception != null) {
                                            LOGGER.error("Failed to commit kafaka offsets:{}", offsets, exception);
                                        }
                                    });
                        } catch (Exception e) {
                            //一个Partition消费异常，继续去消费别的Partition
                            LOGGER.error("Failed to consume kafka message", e);
                        }
                    }
                }
            } catch (WakeupException we) {
                //just ignore shutdown exception
            } catch (Exception e) {
                throw new RuntimeException("Failed to poll kafka message. ApiGateway refused to startup", e);
            } finally {
                consumer.close();
            }
        });
        thread.setName("kafka-consumer-thread");

        thread.start();

    }


    @PreDestroy
    public void destroy() {
        producer.close();
        consumer.wakeup();
        consumer.close();
    }

    public void sendStatisticItem(StatisticItem item) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(Constants.TOPIC_STATISTIC_SAMPLE_EVENT,
                JSON.toJSONString(item));
        send( record );
    }


    private void send( ProducerRecord<String, String> record ) {
        try {
            producerExecutor.submit(() -> {
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Failed to send Kafka message:{}", record, exception);
                    }
                });
            });
        } catch (Exception e ) {
            LOGGER.error("Kafka send message thread pool used up", e );
        }

    }
}
