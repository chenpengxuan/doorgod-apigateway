package com.ymatou.doorgod.apigateway.filter;

import com.ymatou.doorgod.apigateway.integration.KafkaClient;
import com.ymatou.doorgod.apigateway.model.RejectReqEvent;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public abstract class AbstractPreFilter implements PreFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPreFilter.class);

    @Autowired
    private KafkaClient kafkaClient;

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    protected abstract boolean passable(HttpServerRequest req, FilterContext context);

    @Override
    public boolean pass(HttpServerRequest req, FilterContext context) {
        boolean result = passable( req, context );
        if ( !result ) {
            LOGGER.warn("Request:{} is refused by Filter:{}. ruleName:{}, Sample:{}",
                    req.path(), name(), context.ruleName, context.sample);

            RejectReqEvent event = new RejectReqEvent();
            event.setFilterName(name());
            event.setRuleName(context.ruleName);
            event.setSample(context.sample);
            event.setTime(Utils.getCurrentTime());
            event.setUri(req.path());

            kafkaClient.sendRejectReqEvent(event);
        }
        return result;
    }
}
