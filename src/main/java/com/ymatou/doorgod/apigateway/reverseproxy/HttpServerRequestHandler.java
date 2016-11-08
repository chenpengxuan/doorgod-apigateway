package com.ymatou.doorgod.apigateway.reverseproxy;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.cache.UriConfigCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import com.ymatou.doorgod.apigateway.model.StatisticItem;
import com.ymatou.doorgod.apigateway.model.TargetServer;
import com.ymatou.doorgod.apigateway.model.UriConfig;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.DimensionKeyValueFetcher;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FilterContext;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FiltersExecutor;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HeadersAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import rx.Subscriber;

import java.util.Set;

/**
 * 反向代理的核心逻辑
 * Created by tuwenjie on 2016/9/6.
 */
public class HttpServerRequestHandler implements Handler<HttpServerRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerRequestHandler.class);

    private Subscriber<? super Void> subscriber;

    private HttpClient httpClient;



    public HttpServerRequestHandler(Subscriber<? super Void> subscriber, HttpClient httpClient) {
        this.subscriber = subscriber;
        this.httpClient = httpClient;
    }

    @Override
    public void handle(HttpServerRequest httpServerReq) {

        if ( httpServerReq.uri().length() >= HttpServerOptions.DEFAULT_MAX_INITIAL_LINE_LENGTH) {
            LOGGER.error("Detected large uri length {},  uri:{}", httpServerReq.uri().length(), Utils.buildFullUri(httpServerReq));
        }

        //将当前时间放到请求头，以便统计耗时
        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_ACCEEP_TIME, "" + System.currentTimeMillis());

        FiltersExecutor filtersExecutor = VertxVerticleDeployer.filtersExecutor;

        long startTime = System.currentTimeMillis();
        FilterContext filterContext = filtersExecutor.pass(httpServerReq);

        //将Filters耗时放置到报文头
        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_FILTER_CONSUME_TIME,
                "" + (System.currentTimeMillis() - startTime));
        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_SAMPLE,
                JSON.toJSONString(filterContext.sample));
        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_MATCH_RULES,
                filterContext.matchedRuleNames);


        if (filterContext.rejected) {
            Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_REQ_REJECTED_BY_FILTER,
                    "true");
            Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_HIT_RULE,
                    filterContext.hitRuleName);
        }

        process(httpServerReq);

    }

    private void process(HttpServerRequest httpServerReq) {
        if (Utils.containDoorGodHeader(httpServerReq, Constants.HEADER_REQ_REJECTED_BY_FILTER)) {
            httpServerReq.response().setStatusCode(403);
            httpServerReq.response().setStatusMessage("Forbbidden by Api Gateway filters");
            httpServerReq.response().setChunked(true);
            httpServerReq.response().write("Forbbidden by Api Gateway filters");
            onCompleted(httpServerReq);
        } else {
            TargetServer targetServer = VertxVerticleDeployer.targetServer;
            HttpClientRequest forwardClientReq = httpClient.request(httpServerReq.method(), targetServer.getPort(),
                    targetServer.getHost(),
                    httpServerReq.uri(),
                    targetResp -> {

                        //TODO: 100-continue case

                        AppConfig appConfig = VertxVerticleDeployer.appConfig;

                        Utils.addDoorGodHeader(httpServerReq, Constants.HEADER_ORIG_STATUS_CODE, "" + targetResp.statusCode());

                        if ( appConfig.isDebugMode()) {
                            Constants.ACCESS_LOGGER.info("Resp Header:{} {} {}", httpServerReq.path(), System.getProperty("line.separator"), buildHeadersStr(targetResp.headers()));
                        }

                        httpServerReq.response().setStatusCode(targetResp.statusCode());
                        httpServerReq.response().setStatusMessage(targetResp.statusMessage());

                        httpServerReq.response().headers().setAll(targetResp.headers());

                        if ( !httpServerReq.response().headers().contains("Content-Length")) {
                            httpServerReq.response().setChunked(true);
                        }

                        targetResp.handler(data -> {
                            httpServerReq.response().write(data);
                        });

                        targetResp.exceptionHandler(throwable -> {
                            LOGGER.error("Failed to read target service resp.{} {}:{}.", throwable.getMessage(), httpServerReq.method(), Utils.buildFullUri(httpServerReq), throwable);
                            httpServerReq.response().setStatusCode(502);
                            httpServerReq.response().setStatusMessage("Failed to read target service resp");
                            onError(httpServerReq, throwable);
                        });
                        targetResp.endHandler((v) -> {
                            onCompleted(httpServerReq);
                        });

                    });

            forwardClientReq.headers().setAll(clearDoorgodHeads(httpServerReq));

            /**
             * 当<code>httpServerReq.method()</code>为{@link io.vertx.core.http.HttpMethod.OTHER}时,必须使用rawMethod
             * 见{@link io.vertx.core.http.impl.HttpClientRequestImpl#connect(Handler)}
             */
            forwardClientReq.setRawMethod(httpServerReq.rawMethod());

            forwardClientReq.exceptionHandler(throwable -> {
                LOGGER.error("Failed to transfer req.{}.{}:{}", throwable.getMessage(), httpServerReq.method(), Utils.buildFullUri(httpServerReq), throwable);
                if (throwable instanceof java.net.ConnectException) {
                    httpServerReq.response().setStatusCode(503);
                    httpServerReq.response().setStatusMessage("ApiGateway: Failed to connect target server");
                } else if (throwable instanceof java.util.concurrent.TimeoutException) {
                    httpServerReq.response().setStatusCode(504);
                    httpServerReq.response().setStatusMessage("ApiGateway: Target server timeout");
                } else {
                    httpServerReq.response().setStatusCode(503);
                    httpServerReq.response().setStatusMessage("ApiGateway: Exception in requesting target server");
                }

                onError(httpServerReq, new Exception(throwable));

            });

            /**
             * 对于明确设置了超时时间的uri,设定超时时间
             */
            UriConfigCache configCache = VertxVerticleDeployer.uriConfigCache;
            UriConfig config = configCache.locate(httpServerReq.path().toLowerCase());
            if (config != null && config.getTimeout() > 0) {
                forwardClientReq.setTimeout(config.getTimeout());
            }

            httpServerReq.exceptionHandler(ex ->{
                LOGGER.error("Exception in read http req:{}.{}.", ex.getMessage(), Utils.buildFullPath(httpServerReq), ex);
                forwardClientReq.end();
            });

            httpServerReq.handler(data -> {
                forwardClientReq.write(data);
            });

            httpServerReq.endHandler((v) -> forwardClientReq.end());
        }
    }


    public void fallback(HttpServerRequest httpServerReq ) {

        if ( httpServerReq.response().ended()) {
            return;
        }

        httpServerReq.response().setChunked(true);

        HystrixConfig config = VertxVerticleDeployer.hystrixConfigCache.locate(httpServerReq.path().toLowerCase());

        if (config != null && config.getFallbackStatusCode() != null
                && config.getFallbackStatusCode() > 0) {
            httpServerReq.response().setStatusCode(config.getFallbackStatusCode());
            if (config.getFallbackBody() != null) {
                if ( config.getFallbackBody().trim().startsWith("{")) {
                    httpServerReq.response().headers().set("Content-Type","application/json; charset=utf-8");
                }
                httpServerReq.response().write(config.getFallbackBody());
            }
        } else {
            if ( httpServerReq.response().getStatusCode() == 200 ) {
                //如果statusCode还没设，统一设定为403
                httpServerReq.response().setStatusCode(403);
                httpServerReq.response().setStatusMessage("ApiGateway: Forbidden");
            }
            if ( httpServerReq.response().getStatusMessage() != null ) {
                httpServerReq.response().write(httpServerReq.response().getStatusMessage());
            }
        }

        onCompleted(httpServerReq);

    }

    public void onCompleted(HttpServerRequest req) {
        sendStatisticItem(req);
        req.response().end();
        if (subscriber != null) {
            subscriber.onCompleted();
        }
    }

    public void onError(HttpServerRequest req, Throwable t) {
        if (subscriber == null) {
            fallback(req);
        } else {
            //交由Hystrix fallback处理
            subscriber.onError(t);
        }
    }

    public static StatisticItem extract( HttpServerRequest req ) {
        StatisticItem item = new StatisticItem();

        item.setHost(req.headers().get("Host"));
        item.setIp(VertxVerticleDeployer.dimensionKeyValueFetcher.fetch(DimensionKeyValueFetcher.KEY_NAME_IP, req));

        item.setUri(VertxVerticleDeployer.dimensionKeyValueFetcher.fetchUriToStatis(req));

        //时间转换会成为耗CPU的热点方法，此处直接用当前毫秒数，由消费者去转换
        String headerAcceptTime = Utils.getDoorGodHeader(req, Constants.HEADER_ACCEEP_TIME);
        if ( StringUtils.hasText(headerAcceptTime) ) {
            item.setReqTime( headerAcceptTime);
        } else {
            //被Hystrix直接拦的情形下，请求接收时间还没来得及放置到Header
            item.setReqTime("" + System.currentTimeMillis());
        }

        item.setSample(Utils.getDoorGodHeader(req, Constants.HEADER_SAMPLE));

        item.setConsumedTime(System.currentTimeMillis() - Long.valueOf(item.getReqTime()));
        item.setHitRule(Utils.getDoorGodHeader(req, Constants.HEADER_HIT_RULE));
        item.setRejectedByFilter(Boolean.valueOf(Utils.getDoorGodHeader(req, Constants.HEADER_REQ_REJECTED_BY_FILTER)));
        item.setRejectedByHystrix(Boolean.valueOf(Utils.getDoorGodHeader(req, Constants.HEADER_REJECTED_BY_HYSTRIX)));
        item.setStatusCode(req.response().getStatusCode());

        String headerFilterConsumeTime = Utils.getDoorGodHeader(req, Constants.HEADER_FILTER_CONSUME_TIME);
        if ( StringUtils.hasText(headerFilterConsumeTime)) {
            //被Hystrix直接拦的情形下，FilterConsumeTime没有放置到Header
            item.setFilterConsumedTime(Long.valueOf( headerFilterConsumeTime));
        }

        String origStatusCode = Utils.getDoorGodHeader(req, Constants.HEADER_ORIG_STATUS_CODE);
        if ( StringUtils.hasText(origStatusCode)) {
            item.setOrigStatusCode(Integer.valueOf(origStatusCode));
        }

        item.setMatchRules(Utils.getDoorGodHeaderAll(req, Constants.HEADER_MATCH_RULES));

        return item;
    }

    public static void  sendStatisticItem( HttpServerRequest req ) {
        try {
            StatisticItem item = extract(req);

            AppConfig appConfig = VertxVerticleDeployer.appConfig;
            if ( appConfig.isDebugMode()) {
                //Debug模式，输出每个请求处理情况
                Constants.ACCESS_LOGGER.info("Processed:{}, consumed:{}, statusCode:{}, rejectedByFilter:{}, rejectedByHystrix:{}," +
                                "hitRule:{}, origStatusCode:{}, filterConsumed:{}, ip:{}",
                        item.getHost() + item.getUri(), item.getConsumedTime(), item.getStatusCode(),
                        item.isRejectedByFilter(), item.isRejectedByHystrix(), item.getHitRule(),
                        item.getOrigStatusCode(), item.getFilterConsumedTime(), item.getIp());
            } else {
                if ( item.getStatusCode() != 200 || item.isRejectedByFilter() || item.isRejectedByHystrix()) {
                    //只输出非正常处理的请求
                    Constants.ACCESS_LOGGER.warn("Processed:{}, consumed:{}, statusCode:{}, rejectedByFilter:{}, rejectedByHystrix:{}," +
                                    "hitRule:{}, origStatusCode:{}, filterConsumed:{}, ip:{}",
                            item.getHost() + item.getUri(), item.getConsumedTime(), item.getStatusCode(),
                            item.isRejectedByFilter(), item.isRejectedByHystrix(), item.getHitRule(),
                            item.getOrigStatusCode(), item.getFilterConsumedTime(), item.getIp());
                }
            }


            if (appConfig.isDebugMode()) {
                Constants.ACCESS_LOGGER.info("Req Header:{} {} {}", req.path(), System.getProperty("line.separator"), buildHeadersStr(req.headers()));
            }

            VertxVerticleDeployer.kafkaClient.sendStatisticItem(item);
        } catch (Exception t ) {
            //一种保护机制，构造/发送StatisticItem不影响响应的正常返回
            LOGGER.error("Failed to send StatisticItem for req:{}. {}", t.getMessage(), req.host() + req.path(), t);
        }
    }

    public static String buildHeadersStr(MultiMap headers ) {
        StringBuilder sb = new StringBuilder();
        Set<String> names = headers.names();
        for ( String name : names ) {
            sb.append(name).append(":").append(headers.getAll(name)).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }


    public static MultiMap clearDoorgodHeads( HttpServerRequest req ) {
        MultiMap origHeaders = req.headers();
        MultiMap result = null;
        try {
            result = new HeadersAdaptor(new DefaultHttpHeaders());
            Set<String> names = origHeaders.names();
            for ( String name : names ) {
                if ( !name.startsWith(Constants.HEADER_DOOR_GOD_PREFIX)) {
                    result.add(name, origHeaders.getAll(name));
                }
            }
        } catch (Exception e) {
            String fullHeaders = "";
            try {
                fullHeaders = buildHeadersStr(origHeaders);
            } catch (Exception e1) {
                LOGGER.error("Failed to read headers. {}, uri:{}", e1.getMessage(), Utils.buildFullPath(req), e1);
            }
            LOGGER.error("Clear doorgod headers exception. use full headers instead.{}. uri:{}. headers:{}",
                    e.getMessage(), Utils.buildFullPath(req), fullHeaders, e);
            result = origHeaders;
        }

        //与Target Server永远保持Keep-alive
        result.set("Connection", "keep-alive");
        return result;
    };


    public static void forceEnd( HttpServerRequest req) {
        if ( req.response().ended()) {
            return;
        }
        req.response().setChunked(true);
        if ( req.response().getStatusCode() == 200 ) {
            //还没有设错误码，统一设为500
            req.response().setStatusCode(500);
            req.response().setStatusMessage("ApiGateway: Unknown Exception");
        }
        req.response().end();
    }

}

