package com.ymatou.doorgod.apigateway.model;

/**
 * 请求被拒绝时间
 * Created by tuwenjie on 2016/9/8.
 */
public class RejectReqEvent {

    //"yyyyMMdd HH:mm:ss"
    private String time;

    private String filterName;

    private String ruleName;

    private Sample sample;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }
}
