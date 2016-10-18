package com.ymatou.doorgod.apigateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tuwenjie on 2016/9/13.
 */
public interface Constants {

    //访问日志，单独拎出
    public static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("access");

    //请求被拒绝日志，单独拎出
    public static final Logger REJECT_LOGGER = LoggerFactory.getLogger("reject");

    public static final String RULE_TYPE_NAME_LIMIT_TIMES_RULE = "LimitTimesRule";

    public static final String RULE_TYPE_NAME_BLACKLIST_RULE = "BlacklistRule";

    public static final String TOPIC_REJECT_REQ_EVENT = "doorgod.rejectReqEvent";

    public static final String TOPIC_STATISTIC_SAMPLE_EVENT = "doorgod.statisticSampleEvent";

    public static final String TOPIC_UPDATE_OFFENDER_EVENT = "doorgod.updateOffenderEvent";

    public static final String TOPIC_UPDATE_RULE_EVENT = "doorgod.updateRuleEvent";

    public static final String TOPIC_UPDATE_HYSTRIX_CONFIG_EVENT = "doorgod.updateHystrixConfigEvent";

    public static final String TOPIC_UPDATE_URI_CONFIG_EVENT = "doorgod.updateUriConfigEvent";

    public static final String TOPIC_UPDATE_KEY_ALIAS_EVENT = "doorgod.updateKeyAliasEvent";

    public static final String COLLECTION_LIMIT_TIMES_RULE_OFFENDER = "LimitTimesRuleOffender";

    public static final String COLLECTION_BLACLIST_RULE_OFFENDER = "BlacklistRuleOffender";

    public static final String HYSTRIX_COMMAND_KEY_FILTERS_EXECUTOR = "filtersExecutor";

    public static final String HEADER_DOOR_GOD_PREFIX = "$doorgod$-";

    //接收请求时间
    public static final String HEADER_ACCEEP_TIME = "acceptTime";

    //Filter执行时间
    public static final String HEADER_FILTER_CONSUME_TIME = "filterConsumedTime";

    //是否被Filter拦截
    public static final String HEADER_REQ_REJECTED_BY_FILTER = "rejectedByFilter";

    //被拦截的具体规则名
    public static final String HEADER_HIT_RULE = "hitRule";

    //样本
    public static final String HEADER_SAMPLE = "sample";

    //原始响应码
    public static final String HEADER_ORIG_STATUS_CODE = "origStatusCode";

    //是否被Hystrix拦截
    public static final String HEADER_REJECTED_BY_HYSTRIX = "rejectedByHystrix";

    public static final int MAX_CACHED_URIS = 3000;

    public static final int MAX_OFFENDERS = 10000;
}
