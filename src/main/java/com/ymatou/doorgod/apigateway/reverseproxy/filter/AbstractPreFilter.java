package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import com.ymatou.doorgod.apigateway.reverseproxy.VertxVerticleDeployer;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
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
            if ( context.hitRuleName == null ) {
                context.hitRuleName = name( );
            }

            if (VertxVerticleDeployer.appConfig.isDebugMode()) {
                Constants.REJECT_LOGGER.info("Reject {} by ruleName:{}, Sample:{}",
                        Utils.buildFullUri(req),
                        context.hitRuleName,
                        context.sample);
            }

        }
        return result;
    }
}
