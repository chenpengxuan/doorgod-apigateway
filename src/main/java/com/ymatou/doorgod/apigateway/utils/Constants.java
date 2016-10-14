package com.ymatou.doorgod.apigateway.utils;

/**
 * Created by tuwenjie on 2016/9/13.
 */
public interface Constants {
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

    public static final int MAX_CACHED_URIS = 3000;

    public static final int MAX_OFFENDERS = 10000;
}
