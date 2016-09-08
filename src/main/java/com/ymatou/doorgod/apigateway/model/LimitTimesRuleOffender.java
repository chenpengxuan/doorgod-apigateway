package com.ymatou.doorgod.apigateway.model;

import java.util.Date;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public class LimitTimesRuleOffender {

    private Sample sample;

    /**
     * 释放时间, exclusive
     */
    private Date releaseTime;

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }
}
