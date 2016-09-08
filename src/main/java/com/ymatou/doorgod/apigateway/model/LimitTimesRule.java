package com.ymatou.doorgod.apigateway.model;

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
     * 从请求中提取统计样本的groovy脚本
     * 该脚本实现{@link SampleFetcher}接口
     */
    private String sampleFetcherScript;

    /**
     * 根据<code>sampleFetcherScript</code>生成的实例
     */
    private SampleFetcher sampleFetcher;

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

    public String getSampleFetcherScript() {
        return sampleFetcherScript;
    }

    public void setSampleFetcherScript(String sampleFetcherScript) {
        this.sampleFetcherScript = sampleFetcherScript;
    }

    public SampleFetcher getSampleFetcher() {
        return sampleFetcher;
    }

    public void setSampleFetcher(SampleFetcher sampleFetcher) {
        this.sampleFetcher = sampleFetcher;
    }
}
