package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.reverseproxy.filter.PreFilter;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class CustomizeFilterCache implements Cache {

    private List<PreFilter> customizeFilters = new ArrayList<PreFilter>( );

    @Autowired
    private MySqlClient mySqlClient;

    @PostConstruct
    @Override
    public void reload() throws Exception {
        List<PreFilter> loadeds = mySqlClient.loadCustomizeFilters();
        Collections.sort(loadeds, new PreFilter.PreFilterComparator());
        customizeFilters = loadeds;
    }

    public List<PreFilter> getCustomizeFilters() {
        return customizeFilters;
    }

    public void setCustomizeFilters(List<PreFilter> customizeFilters) {
        this.customizeFilters = customizeFilters;
    }
}
