package com.ymatou.doorgod.apigateway.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class BlacklistRule extends AbstractRule {

    /**
     * 用来从http请求提取样本的样本维度KEY
     */
    private Set<String> dimensionKeys = new HashSet<String>( );

    public Set<String> getDimensionKeys() {
        return dimensionKeys;
    }

    public void setDimensionKeys(Set<String> dimensionKeys) {
        this.dimensionKeys = dimensionKeys;
    }

    @Override
    public RuleTypeEnum type() {
        return RuleTypeEnum.BlacklistRule;
    }
}
