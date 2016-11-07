package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.cache.CustomizeFilterCache;
import com.ymatou.doorgod.apigateway.cache.RuleCache;
import com.ymatou.doorgod.apigateway.integration.KafkaClient;
import com.ymatou.doorgod.apigateway.model.HostUri;
import com.ymatou.doorgod.apigateway.model.Sample;
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

    public FilterContext pass(HttpServerRequest httpReq) {
        boolean pass = true;
        Sample sample = null;
        FilterContext context = new FilterContext();
        context.hostUri = new HostUri(httpReq.host(), httpReq.path());
        try {
            Set<String> dimensionKeys = ruleCache.getAllDimensionKeys();
            sample = dimensionKeyValueFetcher.fetch(dimensionKeys, httpReq);

            context.sample = sample;

            //先执行黑名单校验
            pass = blacklistRulesFilter.pass(httpReq, context);

            if (pass) {
                //再执行限次校验
                pass = limitTimesRulesFilter.pass(httpReq, context);

                if (pass) {

                    //执行自定义Filters
                    for (PreFilter filter : customizeFilterCache.getCustomizeFilters()) {
                        pass = filter.pass(httpReq, context);
                        if (!pass) {
                            break;
                        }
                    }
                }
            }

        } catch ( Exception e ) {
            LOGGER.error("Exception in doing filter for request:{}. {}", e.getMessage(), Utils.buildFullUri(httpReq), e );
            context.rejected = false;
            context.hitRuleName = null;
        }

        return context;
    }
}
