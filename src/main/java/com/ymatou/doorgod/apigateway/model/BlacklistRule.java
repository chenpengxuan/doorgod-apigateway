package com.ymatou.doorgod.apigateway.model;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class BlacklistRule extends AbstractRule {

    /**
     * 从请求中提取统计样本的groovy脚本
     * 该脚本实现{@link SampleFetcher}接口
     */
    private String sampleFetcherScript;

    /**
     * 根据<code>sampleFetcherScript</code>生成的实例
     */
    private SampleFetcher sampleFetcher;

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
