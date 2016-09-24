package com.ymatou.doorgod.apigateway.http.hystrix;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixKey;
import com.ymatou.doorgod.apigateway.utils.Constants;

/**
 * 默认{@link com.netflix.hystrix.HystrixCommandKey.Factory}会内部缓存每一个key
 * 当外部用任意构造的不同URI攻击时，会导致{@link com.netflix.hystrix.HystrixCommandKey.Factory}内部缓存无限增长导致OOM
 * 此类用于解决默认Factory的潜在OOM问题
 * Created by tuwenjie on 2016/9/24.
 */
public class MyHystrixCommandKeyFactory {

    private static LoadingCache<String, HystrixCommandKey> cache
            = CacheBuilder.newBuilder()
            .maximumSize(Constants.MAX_CACHED_URIS)
            .build(
                    new CacheLoader<String, HystrixCommandKey>() {
                        public HystrixCommandKey load(String key) {
                            return new MyCommandKey(key);
                        }
                    });;


    private static class MyCommandKey extends HystrixKey.HystrixKeyDefault implements HystrixCommandKey {
        public MyCommandKey(String name) {
            super(name);
        }

        @Override
        public int hashCode() {
            return  name().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if ( obj instanceof  HystrixCommandKey ) {
                return name().equals(((HystrixCommandKey)obj).name());
            }
            return  false;
        }
    }


    public static HystrixCommandKey asKey( String key) {
        return cache.getUnchecked(key);
    }

}
