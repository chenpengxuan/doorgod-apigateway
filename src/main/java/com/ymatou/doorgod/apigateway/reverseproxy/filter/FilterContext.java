package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.model.HostUri;
import com.ymatou.doorgod.apigateway.model.Sample;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public class FilterContext {
    public Sample sample;

    public HostUri hostUri;

    //是否被拒绝
    public boolean rejected;

    //被拒绝的规则名
    public String rejectRuleName;

    public List<String> matchedRuleNames = new ArrayList<String>();

}
