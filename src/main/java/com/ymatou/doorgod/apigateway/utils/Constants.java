package com.ymatou.doorgod.apigateway.utils;

/**
 * Created by tuwenjie on 2016/9/13.
 */
public interface Constants {
    public static final String RULE_TYPE_NAME_LIMIT_TIMES_RULE = "LimitTimesRule";

    public static final String RULE_TYPE_NAME_BLACKLIST_RULE = "BlacklistRule";

    public static final String TOPIC_REJECT_REQ_EVENT = "doorgod.rejectReqEvent";

    public static final String TOPIC_STATISTIC_SAMPLE_EVENT = "doorgod.statisticSampleEvent";

    public static final String TOPIC_OFFENDERS_UPDATE_EVENT = "doorgod.updateOffendersEvent";

    public static final int MAX_CACHED_URIS = 3000;
}
