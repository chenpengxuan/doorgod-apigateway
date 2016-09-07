package com.ymatou.doorgod.apigateway.model;

import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public abstract class AbstractRule implements Ordered {

    private String name;

    /**
     * 越小越靠前执行
     */
    private int order;

    /**
     * 使用范围
     */
    private ScopeEnum scope;

    /**
     * 适用的uri列表。<code>scope</code>为{@link ScopeEnum#SPECIFIC_URIS}时有效
     */
    private Set<String> applicableUris = new HashSet<String>( );

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }

    public Set<String> getApplicableUris() {
        return applicableUris;
    }

    public void setApplicableUris(Set<String> applicableUris) {
        this.applicableUris = applicableUris;
    }
}
