package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.filter.PreFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class CustomizeFilterCache implements Cache {

    private List<PreFilter> customizeFilters = new ArrayList<PreFilter>( );

    @Override
    public void reload() {

    }

    public List<PreFilter> getCustomizeFilters() {
        return customizeFilters;
    }

    public void setCustomizeFilters(List<PreFilter> customizeFilters) {
        this.customizeFilters = customizeFilters;
    }
}
