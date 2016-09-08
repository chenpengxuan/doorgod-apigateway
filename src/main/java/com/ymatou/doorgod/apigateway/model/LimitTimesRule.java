package com.ymatou.doorgod.apigateway.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 限次规则
 * Created by tuwenjie on 2016/9/7.
 */
public class LimitTimesRule extends AbstractRule {

    //统计时间段(秒计)，譬如60s
    private int statisticSpan;

    //统计时间段内，访问次数达到该上限（inclusive），即触发限制
    private long timesCap;

    //达到上限后，拒绝访问的时长，以秒计，譬如300s
    private int rejectionSpan;


    /**
     * 用来从http请求提取样本的样本维度KEY
     */
    private Set<String> dimensionKeys = new HashSet<String>( );

    public int getStatisticSpan() {
        return statisticSpan;
    }

    public void setStatisticSpan(int statisticSpan) {
        this.statisticSpan = statisticSpan;
    }

    public long getTimesCap() {
        return timesCap;
    }

    public void setTimesCap(long timesCap) {
        this.timesCap = timesCap;
    }

    public int getRejectionSpan() {
        return rejectionSpan;
    }

    public void setRejectionSpan(int rejectionSpan) {
        this.rejectionSpan = rejectionSpan;
    }

    public Set<String> getDimensionKeys() {
        return dimensionKeys;
    }

    public void setDimensionKeys(Set<String> dimensionKeys) {
        this.dimensionKeys = dimensionKeys;
    }
}
