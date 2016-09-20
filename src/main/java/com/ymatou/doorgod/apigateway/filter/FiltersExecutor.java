package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.cache.CustomizeFilterCache;
import com.ymatou.doorgod.apigateway.cache.RuleCache;
import com.ymatou.doorgod.apigateway.integration.KafkaClient;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.model.StatisticItem;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class FiltersExecutor {

    private static Logger LOGGER = LoggerFactory.getLogger(FiltersExecutor.class);

    @Autowired
    private BlacklistRulesFilter blacklistRulesFilter;

    @Autowired
    private LimitTimesRulesFilter limitTimesRulesFilter;

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private DimensionKeyValueFetcher dimensionKeyValueFetcher;

    @Autowired
    private KafkaClient kafkaClient;

    @Autowired
    private CustomizeFilterCache customizeFilterCache;

    public boolean pass(HttpServerRequest httpReq) {
        boolean result = true;
        Sample sample = null;
        try {
            FilterContext context = new FilterContext();
            Set<String> dimensionKeys = ruleCache.getAllDimensionKeys();
            sample = dimensionKeyValueFetcher.fetch(dimensionKeys, httpReq);

            context.sample = sample;

            //先执行黑名单校验
            result = blacklistRulesFilter.pass(httpReq, context);

            if (result) {
                //再执行限次校验
                result = limitTimesRulesFilter.pass(httpReq, context);

                if (result) {

                    //执行自定义Filters
                    for (PreFilter filter : customizeFilterCache.getCustomizeFilters()) {
                        result = filter.pass(httpReq, context);
                        if (!result) {
                            break;
                        }
                    }
                }
            }

        } catch ( Exception e ) {
            LOGGER.error("Exception in doing filter for http request:{}", httpReq.path(), e );
            result = true;
        }

        if ( result ) {
            //只有通过的请求，才需要继续累计
            StatisticItem item = new StatisticItem();
            item.setSample(sample);
            item.setReqTime(Utils.getCurrentTime());
            kafkaClient.sendStatisticItem(item);
        }

        return result;
    }
}
