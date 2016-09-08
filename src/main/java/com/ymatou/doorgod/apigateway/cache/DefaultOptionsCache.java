package com.ymatou.doorgod.apigateway.cache;

import com.ymatou.doorgod.apigateway.model.DefaultOptions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by tuwenjie on 2016/9/8.
 */
@Component
public class DefaultOptionsCache implements Cache {

    private DefaultOptions defaultOptions = new DefaultOptions();

    @PostConstruct
    @Override
    public void reload() {

    }


    public DefaultOptions get( ) {
        return defaultOptions;
    }

}
