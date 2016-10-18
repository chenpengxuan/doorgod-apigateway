package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.model.Sample;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public class FilterContext {
    public Sample sample;

    //是否被拒绝
    public boolean rejected;

    //被拒绝的规则名
    public String rejectRuleName;

}
