package com.ymatou.doorgod.apigateway.integration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.Sample;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tuwenjie on 2016/9/22.
 */
@Component
public class MongodbClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(MongodbClient.class);

    @Autowired
    private Vertx vertx;

    @Autowired
    private AppConfig appConfig;

    private MongoClient mongoClient;

    @PostConstruct
    public void init( ) {
        JsonObject config = new JsonObject( );
        config.put("db_name", appConfig.getMongodbName());
        config.put("connection_string", appConfig.getMongodbUrl());
        mongoClient = MongoClient.createShared(vertx, config);
    }


    public Map<Sample, Date> loadLimitTimesRuleOffenders(String ruleName ) throws Exception {

        Map<Sample, Date> result = new HashMap<Sample, Date >();

        FindOptions findOptions = new FindOptions().setLimit(Constants.MAX_OFFENDERS);
        findOptions.setFields(new JsonObject().put("sample",true).put("releaseDate", true));
        JsonObject query = new JsonObject().put("ruleName", ruleName);
        query.put("releaseDate", new JsonObject().put("$gt", Long.valueOf(Utils.getCurrentTime())));


        CountDownLatch latch = new CountDownLatch(1);

        Throwable[] throwables = new Throwable[]{null};


        mongoClient.findWithOptions(Constants.COLLECTION_LIMIT_TIMES_RULE_OFFENDER,
                query,
                findOptions,
                event -> {
                    if ( event.succeeded()) {
                        for ( JsonObject jo : event.result()) {
                            try {
                                Sample sample = JSON.parseObject(jo.getString("sample"), Sample.class);
                                Date releaseDate = Utils.parseDate("" + jo.getLong("releaseDate"));

                                if (result.get(sample) == null) {
                                    result.put(sample, releaseDate);
                                } else {
                                    Date existed = result.get(sample);
                                    if (existed.compareTo(releaseDate) < 0) {
                                        //如果有重复的，取允许访问时间点最大的
                                        result.put(sample, releaseDate);
                                    }
                                }
                            } catch (Exception e ) {
                                LOGGER.error("Failed to parse limit times rule offender:{}. ruleName:{}", jo, ruleName);
                            }
                        }
                    } else {
                        throwables[0] = event.cause();
                    }

                    latch.countDown();
                }
        );

        latch.await();

        if ( throwables[0] != null ) {
            throw new Exception("Failed to load limit times rule offenders", throwables[0]);
        }

        return result;
    }



    public Set<Sample> loadBlacklistRuleOffenders(String ruleName ) throws Exception {

        Set<Sample> result = new HashSet<Sample>();

        FindOptions findOptions = new FindOptions().setLimit(Constants.MAX_OFFENDERS);
        findOptions.setFields(new JsonObject().put("sample",true));
        JsonObject query = new JsonObject().put("ruleName", ruleName);

        CountDownLatch latch = new CountDownLatch(1);

        Throwable[] throwables = new Throwable[]{null};


        mongoClient.findWithOptions(Constants.COLLECTION_BLACLIST_RULE_OFFENDER,
                query,
                findOptions,
                event -> {
                    if ( event.succeeded()) {
                        for ( JsonObject jo : event.result()) {
                            try {
                                Sample sample = JSON.parseObject(jo.getString("sample"), Sample.class);
                                result.add(sample);
                            } catch (Exception e ) {
                                LOGGER.error("Failed to parse blacklist rule offender:{}. ruleName:{}", jo, ruleName);
                            }
                        }
                    } else {
                        throwables[0] = event.cause();
                    }

                    latch.countDown();
                }
        );

        latch.await();

        if ( throwables[0] != null ) {
            throw new Exception("Failed to load blacklist rule offenders", throwables[0]);
        }

        return result;
    }

    @PreDestroy
    public void destroy() {
        mongoClient.close();
    }

    /**
     * 主要用于UnitTest，构造mongo数据
     * @return
     */
    public MongoClient mongo() {
        return mongoClient;
    }
}
