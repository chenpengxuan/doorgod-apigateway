package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.utils.Constants;
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
            context.rejected = true;
            if ( context.rejectRuleName == null ) {
                context.rejectRuleName = name( );
            }
            Constants.REJECT_LOGGER.warn("Request:{} is rejected by ruleName:{}, Sample:{}",
                    req.uri(),
                    context.rejectRuleName,
                    context.sample);

        }
        return result;
    }
}
