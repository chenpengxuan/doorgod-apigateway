package com.ymatou.doorgod.apigateway.model;

import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public abstract class AbstractRule implements Ordered, Comparable<AbstractRule> {

    private String name;

    /**
     * 越小越靠前执行
     */
    private int order;


    /**
     * 适用的uri列表。
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

    public Set<String> getApplicableUris() {
        return applicableUris;
    }

    public void setApplicableUris(Set<String> applicableUris) {
        this.applicableUris = applicableUris;
    }

    @Override
    public int compareTo(AbstractRule o) {
        return order - o.getOrder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractRule that = (AbstractRule) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public boolean applicable( String uri ) {
        if (CollectionUtils.isEmpty(applicableUris)) {
            return true;
        }
        for ( String applicable : applicableUris ) {
            if ( applicable.startsWith(uri)) {
                return true;
            }
        }

        return false;
    }
}
