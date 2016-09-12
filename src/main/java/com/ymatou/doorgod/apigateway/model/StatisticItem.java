package com.ymatou.doorgod.apigateway.model;

/**
 * 发送给决策引擎的统计单元。一个请求一个
 * Created by tuwenjie on 2016/9/9.
 */
public class StatisticItem {

    private Sample sample;

    //请求时间:yyyyMMddHHmmss
    private String reqTime;

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }
}
