package com.ymatou.doorgod.apigateway.integration;

import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.filter.PreFilter;
import com.ymatou.doorgod.apigateway.model.*;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import groovy.lang.GroovyClassLoader;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tuwenjie on 2016/9/12.
 */
@Component
public class MySqlClient {

    private static Logger LOGGER = LoggerFactory.getLogger(MySqlClient.class);
    private AsyncSQLClient client;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Vertx vertx;

    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(MySqlClient.class.getClassLoader());


    public Set[] loadAllRules() throws Exception {
        Set<BlacklistRule> blacklistRules = new TreeSet<BlacklistRule>();
        Set<LimitTimesRule> limitTimesRules = new TreeSet<LimitTimesRule>();
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select name, statistic_span, times_cap,rejection_span, scope, `keys`, groupby_keys, rule_type, `order`, uris from rule where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(entries -> {

                                    try {
                                        if (Constants.RULE_TYPE_NAME_LIMIT_TIMES_RULE.equalsIgnoreCase(entries.getString("rule_type"))) {

                                            LimitTimesRule rule = new LimitTimesRule();
                                            rule.setDimensionKeys(Utils.splitByComma(entries.getString("keys")));
                                            rule.setGroupByKeys(Utils.splitByComma(entries.getString("groupby_keys")));
                                            rule.setRejectionSpan(entries.getInteger("rejection_span"));
                                            rule.setStatisticSpan(entries.getInteger("statistic_span"));
                                            rule.setTimesCap(entries.getLong("times_cap"));
                                            rule.setName(entries.getString("name"));
                                            rule.setOrder(entries.getInteger("order"));
                                            rule.setScope(ScopeEnum.valueOf(entries.getString("scope")));
                                            rule.setApplicableUris(Utils.splitByComma(entries.getString("uris")));
                                            limitTimesRules.add(rule);

                                        } else if (Constants.RULE_TYPE_NAME_BLACKLIST_RULE.equalsIgnoreCase(entries.getString("rule_type"))) {

                                            BlacklistRule rule = new BlacklistRule();
                                            rule.setScope(ScopeEnum.valueOf(entries.getString("scope")));
                                            rule.setOrder(entries.getInteger("order"));
                                            rule.setName(entries.getString("name"));
                                            rule.setDimensionKeys(Utils.splitByComma(entries.getString("keys")));
                                            rule.setApplicableUris(Utils.splitByComma(entries.getString("uris")));
                                            blacklistRules.add(rule);

                                        }
                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding rules from mysql", e);
                                        throwableInLoading[0] = e;
                                        latch.countDown();
                                    }

                                });

                                latch.countDown();

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                                latch.countDown();
                            }
                        });
            } else {
                throwableInLoading[0] = connEvent.cause();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            //just ignore
        }

        if (throwableInLoading[0] != null ) {
            throw new Exception( "Failed to load rules", throwableInLoading[0]);
        }

        Set[] result = new TreeSet[2];
        result[0] = blacklistRules;
        result[1] = limitTimesRules;

        return result;
    }



    public List<PreFilter> loadCustomizeFilters() throws Exception {
        List<PreFilter> result = new ArrayList<PreFilter>( );
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select script from customize_filter where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(entries -> {

                                    try {

                                        String script = entries.getString("script");
                                        Class clazz = groovyClassLoader.parseClass(script);
                                        PreFilter preFilter = (PreFilter) clazz.newInstance();

                                        result.add(preFilter);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding customize filters from mysql", e);
                                        throwableInLoading[0] = e;
                                        latch.countDown();
                                    }

                                });

                                latch.countDown();

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                                latch.countDown();
                            }
                        });
            } else {
                throwableInLoading[0] = connEvent.cause();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            //just ignore
        }

        if (throwableInLoading[0] != null ) {
            throw new Exception( "Failed to load customize filters", throwableInLoading[0]);
        }

        return result;
    }


    public List<HystrixConfig> loadHystrixConfigs() throws Exception {
        List<HystrixConfig> result = new ArrayList<HystrixConfig>( );
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select max_concurrent_reqs, timeout,circuit_breaker_force_open, circuit_breaker_force_close, circuit_breaker_error_threshold, uri, fallback_status_code, fallback_body from hystrix_config where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(entries -> {

                                    try {
                                        HystrixConfig config = new HystrixConfig();
                                        config.setUri(entries.getString("uri"));
                                        config.setErrorThresholdPercentageOfCircuitBreaker(entries.getInteger("circuit_breaker_error_threshold", HystrixConfig.DEFAULT_ERROR_THRESHOLD_PERCENTAGE_CIRCUIT_BREAKER));
                                        config.setFallbackBody(entries.getString("fallback_body"));
                                        config.setFallbackStatusCode(entries.getInteger("fallback_status_code", -1));
                                        config.setForceCircuitBreakerClose(convertBool(entries.getInteger("circuit_breaker_force_close")));
                                        config.setForceCircuitBreakerOpen(convertBool(entries.getInteger("circuit_breaker_force_open")));
                                        config.setMaxConcurrentReqs(entries.getInteger("max_concurrent_reqs", -1));
                                        config.setTimeout(entries.getInteger("timeout", -1));


                                        result.add(config);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding hystrix config from mysql", e);
                                        throwableInLoading[0] = e;
                                        latch.countDown();
                                    }

                                });

                                latch.countDown();

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                                latch.countDown();
                            }
                        });
            } else {
                throwableInLoading[0] = connEvent.cause();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            //just ignore
        }

        if (throwableInLoading[0] != null ) {
            throw new Exception( "Failed to load hystrix config", throwableInLoading[0]);
        }

        return result;
    }


    public List<KeyAlias> loadKeyAliases() throws Exception {
        List<KeyAlias> result = new ArrayList<KeyAlias>( );
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select origin_key_name, alias, uri from uri_key_alias where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(entries -> {

                                    try {
                                        KeyAlias alias = new KeyAlias();
                                        alias.setUri(entries.getString("uri"));
                                        alias.setKey(entries.getString("origin_key_name"));
                                        alias.setAlias(entries.getString("alias"));

                                        result.add(alias);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding key aliases from mysql", e);
                                        throwableInLoading[0] = e;
                                        latch.countDown();
                                    }

                                });

                                latch.countDown();

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                                latch.countDown();
                            }
                        });
            } else {
                throwableInLoading[0] = connEvent.cause();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            //just ignore
        }

        if (throwableInLoading[0] != null ) {
            throw new Exception( "Failed to load key aliases", throwableInLoading[0]);
        }

        return result;
    }



    @PostConstruct
    public void init() {

        JsonObject mySqlClientConfig = new JsonObject().put("host", appConfig.getMySqlHost())
                .put("port", appConfig.getMySqlPort())
                .put("username", appConfig.getMySqlUser())
                .put("password", appConfig.getMySqlPassword())
                .put("database", appConfig.getMySqlDbName());

        client = MySQLClient.createShared(vertx, mySqlClientConfig);
    }

    @PreDestroy
    public void destroy() {
        client.close();
    }

    private boolean convertBool( Integer value ) {
        return value != null && value > 0;
    }
}
