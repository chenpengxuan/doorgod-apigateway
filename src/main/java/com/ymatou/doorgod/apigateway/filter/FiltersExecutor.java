package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.cache.CustomizeDimensionKeyValueFetcherCache;
import com.ymatou.doorgod.apigateway.cache.CustomizeFilterCache;
import com.ymatou.doorgod.apigateway.cache.RuleCache;
import com.ymatou.doorgod.apigateway.model.Sample;
import io.vertx.core.http.HttpServerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class FiltersExecutor {

    @Autowired
    private BlacklistRulesFilter blacklistRulesFilter;

    @Autowired
    private LimitTimesRulesFilter limitTimesRulesFilter;

    @Autowired
    private RuleCache ruleCache;

    @Autowired
    private CustomizeDimensionKeyValueFetcherCache customizeDimensionKeyValueFetcherCache;

    @Autowired
    private CustomizeFilterCache customizeFilterCache;

    public boolean pass(HttpServerRequest httpReq) {

        FilterContext context = new FilterContext();
        Set<String> dimensionKeys = ruleCache.getAllDimensionKeys();
        Sample sample = customizeDimensionKeyValueFetcherCache.getFetcher().fetch(dimensionKeys, httpReq);

        context.sample = sample;

        //先执行黑名单校验
        boolean result = blacklistRulesFilter.pass(httpReq, context);

        if ( result ) {
            //再执行限次校验
            result = limitTimesRulesFilter.pass(httpReq, context);

            if ( result ) {

                //执行自定义Filters
                for (PreFilter filter : customizeFilterCache.getCustomizeFilters()) {
                    result = filter.pass(httpReq, context);
                    if ( !result ) {
                        break;
                    }
                }
            }
        }

        return result;
    }
}
