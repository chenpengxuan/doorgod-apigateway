package com.ymatou.doorgod.apigateway.filter;

import io.vertx.core.http.HttpServerRequest;
import org.springframework.core.Ordered;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public interface PreFilter extends Ordered {

    String name( );

    /**
     * http请求是否被接受
     * @param req
     * @return
     */
    boolean pass(HttpServerRequest req, FilterContext context );
}
