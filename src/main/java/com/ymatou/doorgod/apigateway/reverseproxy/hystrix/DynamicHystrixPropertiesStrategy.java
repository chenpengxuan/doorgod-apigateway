package com.ymatou.doorgod.apigateway.reverseproxy.hystrix;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesCommandDefault;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import com.ymatou.doorgod.apigateway.cache.Cache;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import com.ymatou.doorgod.apigateway.utils.BeanUtils;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * Hystrix动态配置实现
 * Created by tuwenjie on 2016/9/23.
 */
@Component
public class DynamicHystrixPropertiesStrategy extends HystrixPropertiesStrategy implements Cache {

    private static Logger logger = LoggerFactory.getLogger(DynamicHystrixPropertiesStrategy.class);

    @Autowired
    private HystrixConfigCache hystrixConfigCache;


    private LoadingCache<HystrixCommandKey, HystrixCommandProperties> commandKeyToProperties;

    @PostConstruct
    public void init( ) throws Exception {
        reload();
        HystrixPlugins.getInstance().registerPropertiesStrategy(this);
    }


    @Override
    public HystrixCommandProperties getCommandProperties(HystrixCommandKey commandKey, HystrixCommandProperties.Setter builder) {
        if (commandKey.name().equalsIgnoreCase(Constants.HYSTRIX_COMMAND_KEY_FILTERS_EXECUTOR)) {
            return super.getCommandProperties(commandKey, builder);
        }
        return commandKeyToProperties.getUnchecked(commandKey);
    }

    @Override
    public String getCommandPropertiesCacheKey(HystrixCommandKey commandKey, HystrixCommandProperties.Setter builder) {
        if (commandKey.name().equalsIgnoreCase(Constants.HYSTRIX_COMMAND_KEY_FILTERS_EXECUTOR)) {
            return super.getCommandPropertiesCacheKey(commandKey, builder);
        } else {
            //用自己的缓存方案
            return null;
        }
    }

    public static HystrixCommandProperties.Setter build(HystrixConfig config ) {
        HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter();

        setter.withExecutionTimeoutEnabled(false);
        setter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE);

        setter.withRequestLogEnabled(false);

        if ( config != null ) {

            setter.withExecutionIsolationSemaphoreMaxConcurrentRequests(config.getMaxConcurrentReqs());

            setter.withCircuitBreakerForceOpen(config.getForceCircuitBreakerOpen());

            setter.withCircuitBreakerForceClosed(config.getForceCircuitBreakerClose());

            setter.withCircuitBreakerErrorThresholdPercentage(config.getErrorThresholdPercentageOfCircuitBreaker());
            //timeout属性已经在Vertx httpclient设定，Hystrix无需再设
        }

        return setter;
    }

    @Override
    public void reload() throws Exception {
        if (commandKeyToProperties == null) {
            commandKeyToProperties = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<HystrixCommandKey, HystrixCommandProperties>() {
                                public HystrixCommandProperties load(HystrixCommandKey key ) {
                                    HystrixConfig config = hystrixConfigCache.locate(key.name());
                                    return new HystrixPropertiesCommandDefault(key, build(config));
                                }
                            });
        }
        commandKeyToProperties.asMap().forEach((hystrixCommandKey, hystrixCommandProperties) -> {
            HystrixConfig config = hystrixConfigCache.locate(hystrixCommandKey.name());

            if( null != config){

                reloadCircuitBreakerForceOpen(config,hystrixCommandProperties);

                reloadCircuitBreakerForceClosed(config,hystrixCommandProperties);

                reloadCircuitBreakerErrorThresholdPercentage(config,hystrixCommandProperties);

                reloadMaxConcurrentReqs(config,hystrixCommandProperties,hystrixCommandKey);
            }
        });
    }

    /**
     * 重新设置 断路器强制打开属性
     * @param config
     * @param hystrixCommandProperties
     */
    private void reloadCircuitBreakerForceOpen(HystrixConfig config,HystrixCommandProperties hystrixCommandProperties){
        if(!hystrixCommandProperties.circuitBreakerForceOpen().get().equals(config.getForceCircuitBreakerOpen())){
            try {
                BeanUtils.forceSetProperty(hystrixCommandProperties,"circuitBreakerForceOpen",
                        HystrixProperty.Factory.asProperty(config.getForceCircuitBreakerOpen()));
            } catch (Exception e) {
                logger.error("reload circuitBreakerForceOpen error",e);
            }
        }
    }

    /**
     * 重新设置 断路器强制关闭属性
     * @param config
     * @param hystrixCommandProperties
     */
    private void reloadCircuitBreakerForceClosed(HystrixConfig config,HystrixCommandProperties hystrixCommandProperties){
        if(!hystrixCommandProperties.circuitBreakerForceClosed().get().equals(config.getForceCircuitBreakerClose())){
            try {
                BeanUtils.forceSetProperty(hystrixCommandProperties,"circuitBreakerForceClosed",
                        HystrixProperty.Factory.asProperty(config.getForceCircuitBreakerClose()));
            } catch (Exception e) {
                logger.error("reload circuitBreakerForceOpen error",e);
            }
        }
    }

    /**
     * 重新设置 断路器错误百分比
     * @param config
     * @param hystrixCommandProperties
     */
    private void reloadCircuitBreakerErrorThresholdPercentage(HystrixConfig config,HystrixCommandProperties hystrixCommandProperties){
        if(!hystrixCommandProperties.circuitBreakerErrorThresholdPercentage().get()
                .equals(config.getErrorThresholdPercentageOfCircuitBreaker())){
            try {
                BeanUtils.forceSetProperty(hystrixCommandProperties,"circuitBreakerErrorThresholdPercentage",
                        HystrixProperty.Factory.asProperty(config.getErrorThresholdPercentageOfCircuitBreaker()));
            } catch (Exception e) {
                logger.error("reload circuitBreakerForceOpen error",e);
            }
        }
    }


    /**
     * 重新设置 最大并发量
     * @param config
     * @param hystrixCommandProperties
     * @param hystrixCommandKey
     */
    private void reloadMaxConcurrentReqs(HystrixConfig config,HystrixCommandProperties hystrixCommandProperties,HystrixCommandKey hystrixCommandKey){
        //处理 最大并发量
        if (!hystrixCommandProperties.executionIsolationSemaphoreMaxConcurrentRequests().get()
                .equals(config.getMaxConcurrentReqs())) {
            try {
                BeanUtils.forceSetProperty(hystrixCommandProperties,"executionIsolationSemaphoreMaxConcurrentRequests",
                        HystrixProperty.Factory.asProperty(config.getMaxConcurrentReqs()));
            } catch (Exception e) {
                logger.error("reload circuitBreakerForceOpen error",e);
            }
            HystrixForwardReqCommand.removeCommandKey(hystrixCommandKey.name());
        }
    }
}
