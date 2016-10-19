package com.ymatou.doorgod.apigateway.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tuwenjie on 2016/9/8.
 */
public interface Cache {

    static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    void reload( ) throws Exception;
}
