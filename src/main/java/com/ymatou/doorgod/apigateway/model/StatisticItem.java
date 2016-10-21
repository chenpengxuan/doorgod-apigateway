package com.ymatou.doorgod.apigateway.model;

import java.util.List;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 发送给决策引擎的统计单元。一个http请求一个
 * Created by tuwenjie on 2016/9/9.
 */
public class StatisticItem {

    private String uri;

    /**
     * {@link Sample}的json
     */
    private String sample;

    //请求时间:请求接收时刻的毫秒数
    private String reqTime;

    //耗时:以毫秒为单位
    private long consumedTime;

    //http响应码
    private int statusCode;

    private boolean rejectedByFilter;

    private boolean rejectedByHystrix;

    private String hitRule;

    private long filterConsumedTime;

    private int origStatusCode;

    private String host;

    private String ip;

    private List<String> matchRules = new ArrayList<String>();

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    public long getConsumedTime() {
        return consumedTime;
    }

    public void setConsumedTime(long consumedTime) {
        this.consumedTime = consumedTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isRejectedByFilter() {
        return rejectedByFilter;
    }

    public void setRejectedByFilter(boolean rejectedByFilter) {
        this.rejectedByFilter = rejectedByFilter;
    }

    public boolean isRejectedByHystrix() {
        return rejectedByHystrix;
    }

    public void setRejectedByHystrix(boolean rejectedByHystrix) {
        this.rejectedByHystrix = rejectedByHystrix;
    }

    public String getHitRule() {
        return hitRule;
    }

    public void setHitRule(String hitRule) {
        this.hitRule = hitRule;
    }

    public long getFilterConsumedTime() {
        return filterConsumedTime;
    }

    public void setFilterConsumedTime(long filterConsumedTime) {
        this.filterConsumedTime = filterConsumedTime;
    }

    public int getOrigStatusCode() {
        return origStatusCode;
    }

    public void setOrigStatusCode(int origStatusCode) {
        this.origStatusCode = origStatusCode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<String> getMatchRules() {
        return matchRules;
    }

    public void setMatchRules(List<String> matchRules) {
        this.matchRules = matchRules;
    }
}
