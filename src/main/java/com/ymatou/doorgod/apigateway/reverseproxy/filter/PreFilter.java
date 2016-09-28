package com.ymatou.doorgod.apigateway.reverseproxy.filter;

import io.vertx.core.http.HttpServerRequest;
import org.springframework.core.Ordered;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public interface PreFilter extends Ordered  {

    String name( );

    /**
     * http请求是否被接受
     * @param req
     * @return
     */
    boolean pass(HttpServerRequest req, FilterContext context );

    public static class PreFilterComparator implements Comparator<PreFilter>, Serializable  {
        @Override
        public int compare(PreFilter o1, PreFilter o2) {
            return o1.getOrder() - o2.getOrder();
        }
    }
}
