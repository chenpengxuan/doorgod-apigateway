package com.ymatou.doorgod.apigateway.integration;

import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.*;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.PreFilter;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import groovy.lang.GroovyClassLoader;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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


    public Set<AbstractRule> loadAllRules() throws Exception {
        Set<AbstractRule> result = new HashSet<AbstractRule>();
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select name, statistic_span, times_cap,rejection_span,`keys`, groupby_keys, rule_type, `order`, uris, observe_mode from rule where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(row -> {

                                    try {
                                        if (Constants.RULE_TYPE_NAME_LIMIT_TIMES_RULE.equalsIgnoreCase(row.getString("rule_type"))) {

                                            LimitTimesRule rule = new LimitTimesRule();
                                            rule.setDimensionKeys(Utils.splitByComma(row.getString("keys")));
                                            rule.setGroupByKeys(Utils.splitByComma(row.getString("groupby_keys")));
                                            rule.setRejectionSpan(row.getInteger("rejection_span"));
                                            rule.setStatisticSpan(row.getInteger("statistic_span"));
                                            rule.setTimesCap(row.getLong("times_cap"));
                                            rule.setName(row.getString("name"));
                                            rule.setOrder(row.getInteger("order"));
                                            rule.setObserverMode(convertBool(row.getInteger("observe_mode")));
                                            rule.setApplicableUris(Utils.splitByComma(row.getString("uris")));
                                            result.add(rule);

                                        } else if (Constants.RULE_TYPE_NAME_BLACKLIST_RULE.equalsIgnoreCase(row.getString("rule_type"))) {

                                            BlacklistRule rule = new BlacklistRule();
                                            rule.setOrder(row.getInteger("order"));
                                            rule.setName(row.getString("name"));
                                            rule.setDimensionKeys(Utils.splitByComma(row.getString("keys")));
                                            rule.setObserverMode(convertBool(row.getInteger("observe_mode")));
                                            rule.setApplicableUris(Utils.splitByComma(row.getString("uris")));
                                            result.add(rule);

                                        } else {
                                            LOGGER.error("Unknown rule type {} in mysql rule tables", row.getString("rule_type"));
                                        }
                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding rule from mysql. row:{}", row, e);
                                    }

                                });

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                            }
                            connEvent.result().close();
                            latch.countDown();
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
                                queryEvent.result().getRows().stream().forEach(row -> {

                                    try {

                                        String script = row.getString("script");
                                        Class clazz = groovyClassLoader.parseClass(script);
                                        PreFilter preFilter = (PreFilter) clazz.newInstance();

                                        result.add(preFilter);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding customize filters from mysql", e);
                                    }

                                });

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                            }
                            connEvent.result().close();
                            latch.countDown();
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
                connEvent.result().query("select max_concurrent_reqs, circuit_breaker_force_open, circuit_breaker_force_close, circuit_breaker_error_threshold, uri, fallback_status_code, fallback_body from hystrix_config where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(row -> {

                                    try {
                                        HystrixConfig config = new HystrixConfig();
                                        config.setUri(row.getString("uri").toLowerCase());
                                        config.setErrorThresholdPercentageOfCircuitBreaker(
                                                replaceNullAndZeroWithDefault(row.getInteger("circuit_breaker_error_threshold"),
                                                        HystrixConfig.DEFAULT_ERROR_THRESHOLD_PERCENTAGE_CIRCUIT_BREAKER));
                                        config.setFallbackBody(row.getString("fallback_body"));
                                        config.setFallbackStatusCode(row.getInteger("fallback_status_code", -1));
                                        config.setForceCircuitBreakerClose(convertBool(row.getInteger("circuit_breaker_force_close")));
                                        config.setForceCircuitBreakerOpen(convertBool(row.getInteger("circuit_breaker_force_open")));
                                        config.setMaxConcurrentReqs(replaceNullAndZeroWithDefault(row.getInteger("max_concurrent_reqs"), Integer.MAX_VALUE));

                                        result.add(config);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding hystrix config from mysql", e);
                                    }

                                });

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                            }
                            connEvent.result().close();
                            latch.countDown();
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

    public List<UriConfig> loadUriConfigs() throws Exception {
        List<UriConfig> result = new ArrayList<UriConfig>( );
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select timeout, uri from uri_config where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(row -> {

                                    try {
                                        UriConfig config = new UriConfig();
                                        config.setUri(row.getString("uri").toLowerCase());
                                        config.setTimeout(row.getInteger("timeout"));

                                        result.add(config);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding uri config from mysql", e);
                                    }

                                });

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                            }
                            connEvent.result().close();
                            latch.countDown();
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
            throw new Exception( "Failed to load uri config", throwableInLoading[0]);
        }

        return result;
    }


    public Set<KeyAlias> loadKeyAliases() throws Exception {
        Set<KeyAlias> result = new HashSet<KeyAlias>( );
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().query("select origin_key_name, alias, uri from uri_key_alias where status='ENABLE' ",
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(row -> {

                                    try {
                                        KeyAlias alias = new KeyAlias();
                                        alias.setUri(row.getString("uri").toLowerCase());
                                        alias.setKey(row.getString("origin_key_name"));
                                        alias.setAlias(row.getString("alias"));

                                        result.add(alias);

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loding key aliases from mysql", e);
                                    }

                                });

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                            }
                            connEvent.result().close();
                            latch.countDown();
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

    public TargetServer locateTargetServer() throws Exception {
        TargetServer result = new TargetServer();
        JsonArray params = new JsonArray().add(Utils.localIp());
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] throwableInLoading = new Throwable[]{null};
        client.getConnection(connEvent -> {
            if (connEvent.succeeded()) {
                connEvent.result().queryWithParams("select target_server, port from target_server where status='ENABLE' and gateway_ip=?",
                        params,
                        queryEvent -> {
                            if (queryEvent.succeeded()) {
                                queryEvent.result().getRows().stream().forEach(row -> {
                                    try {
                                        if ( StringUtils.hasText(row.getString("target_server"))
                                            && row.getInteger("port", -1) > 0) {
                                            result.setHost(row.getString("target_server").trim());
                                            result.setPort(row.getInteger("port"));
                                        }

                                    } catch (Exception e) {
                                        LOGGER.error("Exception in loading key aliases from mysql", e);
                                    }

                                });

                            } else {
                                throwableInLoading[0] = queryEvent.cause();
                            }
                            connEvent.result().close();
                            latch.countDown();
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
            throw new Exception( "Failed to load target server", throwableInLoading[0]);
        }

        if ( !StringUtils.hasText(result.getHost()) || result.getPort() <= 0){
            throw new Exception("Target server not set properly for ApiGateway on ip:" + Utils.localIp());
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

    private Integer replaceNullAndZeroWithDefault(Integer value,Integer defaultValue){
        return (value == null || value == 0) ? defaultValue:value;
    }


}
