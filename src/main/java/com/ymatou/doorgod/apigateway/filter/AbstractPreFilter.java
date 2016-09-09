package com.ymatou.doorgod.apigateway.filter;

import io.vertx.core.http.HttpServerRequest;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public abstract class AbstractPreFilter implements PreFilter {

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    protected abstract boolean passable(HttpServerRequest req, FilterContext context);

    @Override
    public boolean pass(HttpServerRequest req, FilterContext context) {
        boolean result = passable( req, context );
        if ( !result ) {
            //todo: 发送拒绝请求事件
        }
        return result;
    }
}
