package com.ymatou.doorgod.apigateway.cache;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.integration.KafkaRecordListener;
import com.ymatou.doorgod.apigateway.reverseproxy.hystrix.DynamicHystrixPropertiesStrategy;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private RuleOffenderCache ruleOffenderCache;

    @Autowired
    private HystrixConfigCache hystrixConfigCache;

    @Autowired
    private KeyAliasCache keyAliasCache;

    @Autowired
    private UriConfigCache uriConfigCache;

    @Autowired
    private UriPatternCache uriPatternCache;

    @Autowired
    private DynamicHystrixPropertiesStrategy dynamicHystrixPropertiesStrategy;


    private Map<Offset, AtomicInteger> failures = new ConcurrentHashMap<Offset, AtomicInteger>();

    public static final int MAX_FAILURES = 100;


    @Override
    public void onRecordReceived(ConsumerRecord<String, String> record) throws Exception {
        try {
            switch (record.topic()) {
                case Constants.TOPIC_UPDATE_OFFENDER_EVENT:
                    UpdateOffenderEvent event = JSON.parseObject(record.value(), UpdateOffenderEvent.class);
                    ruleOffenderCache.reload(event.getRuleName());
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
                case Constants.TOPIC_UPDATE_URI_CONFIG_EVENT:
                    uriConfigCache.reload();
                    break;
                case Constants.TOPIC_UPDATE_URI_PATTERN_EVENT:
                    uriPatternCache.reload();
                    break;
                default:
                    LOGGER.error("Receive unknow kafka message:{}", record);
            }
        } catch ( Exception e ) {
            //记录下具体消费那条日志失败
            LOGGER.error("Failed to consume kafka message:{}. {}", record, e.getMessage(), e);
            Offset offset = Offset.newInstance(record);
            failures.putIfAbsent(offset, new AtomicInteger(1));

            AtomicInteger failed = failures.get(offset);

            if ( failed.get() > MAX_FAILURES) {
                failures.remove(offset);

                //超过最大失败次数，不抛异常，当作消费成功
                return;
            } else {

                //失败次数+1, 抛异常，等待重试
                failures.get(offset).incrementAndGet();
                throw e;
            }
        }
    }

    public static class Offset {
        private String topic;
        private int partition;
        private long offset;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getPartition() {
            return partition;
        }

        public void setPartition(int partition) {
            this.partition = partition;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Offset offset1 = (Offset) o;

            if (partition != offset1.partition) return false;
            if (offset != offset1.offset) return false;
            return topic != null ? topic.equals(offset1.topic) : offset1.topic == null;

        }

        @Override
        public int hashCode() {
            int result = topic != null ? topic.hashCode() : 0;
            result = 31 * result + partition;
            result = 31 * result + (int) (offset ^ (offset >>> 32));
            return result;
        }


        public static Offset newInstance( ConsumerRecord record ) {
            Offset result = new Offset();
            result.setOffset(record.offset());
            result.setPartition(record.partition());
            result.setTopic(record.topic());

            return result;
        }

    }

}
