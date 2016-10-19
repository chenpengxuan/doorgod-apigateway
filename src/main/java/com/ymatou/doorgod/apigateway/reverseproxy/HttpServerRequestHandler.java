package com.ymatou.doorgod.apigateway.reverseproxy;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.cache.UriConfigCache;
import com.ymatou.doorgod.apigateway.cache.UriPatternCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.integration.KafkaClient;
import com.ymatou.doorgod.apigateway.model.*;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.DimensionKeyValueFetcher;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FiltersExecutor;
import com.ymatou.doorgod.apigateway.reverseproxy.hystrix.HystrixFiltersExecutorCommand;
import com.ymatou.doorgod.apigateway.utils.Constants;
import com.ymatou.doorgod.apigateway.utils.Utils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private Subscriber<? super Void> subscriber;

    private HttpClient httpClient;

    private DimensionKeyValueFetcher dimensionKeyValueFetcher;


    public HttpServerRequestHandler(Subscriber<? super Void> subscriber, HttpClient httpClient) {
        this.subscriber = subscriber;
        this.httpClient = httpClient;
        this.dimensionKeyValueFetcher = SpringContextHolder.getBean(DimensionKeyValueFetcher.class);

    }

    @Override
    public void handle(HttpServerRequest httpServerReq) {

        //将当前时间放到请求头，以便统计耗时
        httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_ACCEEP_TIME), "" + System.currentTimeMillis());

        FiltersExecutor filtersExecutor = SpringContextHolder.getBean(FiltersExecutor.class);

        //通过Hystrix监控FiltersExecutor性能及TPS等
        HystrixFiltersExecutorCommand filtersExecutorCommand = new HystrixFiltersExecutorCommand(filtersExecutor, httpServerReq);
        long startTime = System.currentTimeMillis();
        filtersExecutorCommand.toObservable().subscribe(filterContext -> {

            httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_SAMPLE),
                    JSON.toJSONString(filterContext.sample));

            //将Filters耗时放置到报文头
            httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_FILTER_CONSUME_TIME),
                    "" + (System.currentTimeMillis() - startTime));
            if (filterContext.rejected) {
                httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_REQ_REJECTED_BY_FILTER),
                        "true");
                httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_HIT_RULE),
                        filterContext.rejectRuleName);
            }

        });

        process(httpServerReq);

    }

    private void process(HttpServerRequest httpServerReq) {
        if (httpServerReq.headers().contains(Utils.buildFullDoorGodHeaderName(Constants.HEADER_REQ_REJECTED_BY_FILTER))) {
            fallback(httpServerReq, "Rejected by filters");
        } else {
            TargetServer targetServer = VertxVerticleDeployer.targetServer;
            HttpClientRequest forwardClientReq = httpClient.request(httpServerReq.method(), targetServer.getPort(),
                    targetServer.getHost(),
                    httpServerReq.uri(),
                    targetResp -> {

                        //TODO: 100-continue case

                        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);

                        httpServerReq.headers().add(Utils.buildFullDoorGodHeaderName(Constants.HEADER_ORIG_STATUS_CODE), "" + targetResp.statusCode());

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
                            LOGGER.error("Failed to read target service resp {}:{}", httpServerReq.method(), httpServerReq.uri(), throwable);
                            httpServerReq.response().setStatusCode(500);
                            httpServerReq.response().setStatusMessage("Failed to read target service resp");
                            onError(httpServerReq, throwable);
                        });
                        targetResp.endHandler((v) -> {
                            onCompleted(httpServerReq);
                        });
                    });

            forwardClientReq.headers().setAll(clearDoorgodHeads(httpServerReq.headers()));

            /**
             * 对于明确设置了超时时间的uri,设定超时时间
             */
            UriConfigCache configCache = SpringContextHolder.getBean(UriConfigCache.class);
            UriConfig config = configCache.locate(httpServerReq.path().toLowerCase());
            if (config != null && config.getTimeout() > 0) {
                forwardClientReq.setTimeout(config.getTimeout());
            }


            httpServerReq.handler(data -> {
                forwardClientReq.write(data);
            });

            forwardClientReq.exceptionHandler(throwable -> {
                LOGGER.error("Failed to transfer reverseproxy req {}:{}", httpServerReq.method(), httpServerReq.uri(), throwable);
                httpServerReq.response().setChunked(true);
                if (throwable instanceof java.net.ConnectException) {
                    httpServerReq.response().setStatusCode(408);
                    httpServerReq.response().write("ApiGateway:failed to connect target service");
                } else if (throwable instanceof java.util.concurrent.TimeoutException) {
                    httpServerReq.response().setStatusCode(504);
                    httpServerReq.response().write("ApiGateway:target service timeout");
                } else {
                    httpServerReq.response().setStatusCode(503);
                    httpServerReq.response().write("ApiGateway:Exception in forwarding request");
                }

                onError(httpServerReq, new Exception(throwable));

            });

            httpServerReq.endHandler((v) -> forwardClientReq.end());
        }
    }


    public void fallback(HttpServerRequest httpServerReq, String reason) {

        httpServerReq.response().setChunked(true);

        HystrixConfigCache configCache = SpringContextHolder.getBean(HystrixConfigCache.class);

        HystrixConfig config = configCache.locate(httpServerReq.path().toLowerCase());

        if (config != null && config.getFallbackStatusCode() != null
                && config.getFallbackStatusCode() > 0) {
            httpServerReq.response().setStatusCode(config.getFallbackStatusCode());
            if (config.getFallbackBody() != null) {
                httpServerReq.response().write(config.getFallbackBody());
            }
        } else {
            httpServerReq.response().setStatusCode(403);
            httpServerReq.response().write("ApiGateway: request is rejected." + reason);
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
            sendStatisticItem(req);
            req.response().end();
        } else if (subscriber != null) {
            //交由Hystrix fallback处理
            subscriber.onError(t);
        }
    }

    public StatisticItem extract( HttpServerRequest req ) {
        StatisticItem item = new StatisticItem();

        item.setHost(req.headers().get("Host"));
        item.setIp(dimensionKeyValueFetcher.fetch(DimensionKeyValueFetcher.KEY_NAME_IP, req));

        item.setUri(dimensionKeyValueFetcher.fetchUriToStatis(req));

        item.setReqTime(Utils.getTimeStr(Long.valueOf(req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_ACCEEP_TIME)))));

        String sampleStr = req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_SAMPLE));
        if (StringUtils.hasText(sampleStr)) {
            item.setSample(JSON.parseObject(sampleStr, Sample.class));
        }

        item.setConsumedTime(System.currentTimeMillis() - Long.valueOf(req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_ACCEEP_TIME))));
        item.setHitRule(req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_HIT_RULE)));
        item.setRejectedByFilter(Boolean.valueOf(req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_REQ_REJECTED_BY_FILTER))));
        item.setRejectedByHystrix(Boolean.valueOf(req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_REJECTED_BY_HYSTRIX))));
        item.setStatusCode(req.response().getStatusCode());
        item.setFilterConsumedTime(Long.valueOf(req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_FILTER_CONSUME_TIME))));

        String origStatusCode = req.headers().get(Utils.buildFullDoorGodHeaderName(Constants.HEADER_ORIG_STATUS_CODE));
        if ( StringUtils.hasText(origStatusCode)) {
            item.setOrigStatusCode(Integer.valueOf(origStatusCode));
        }
        return item;
    }

    public void sendStatisticItem( HttpServerRequest req ) {

        StatisticItem item = extract(req);

        Constants.ACCESS_LOGGER.info("Processed:{}, consumed:{}, statusCode:{}, rejectedByFilter:{}, rejectedByHystrix:{}," +
                "hitRule:{}, origStatusCode:{}, filterConsumed:{}, ip:{}",
                 item.getHost() + item.getUri(), item.getConsumedTime(), item.getStatusCode(),
                item.isRejectedByFilter(), item.isRejectedByHystrix(), item.getHitRule(),
                item.getOrigStatusCode(), item.getFilterConsumedTime(), item.getIp());

        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);
        if ( appConfig.isDebugMode()) {
            Constants.ACCESS_LOGGER.info("Req Header:{} {} {}", req.path(), System.getProperty("line.separator"), buildHeadersStr(req.headers()));
        }

        KafkaClient kafkaClient = SpringContextHolder.getBean(KafkaClient.class);
        kafkaClient.sendStatisticItem(item);
    }

    public String buildHeadersStr(MultiMap headers ) {
        StringBuilder sb = new StringBuilder();
        Set<String> names = headers.names();
        for ( String name : names ) {
            sb.append(name).append(":").append(headers.getAll(name)).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }


    public MultiMap clearDoorgodHeads( MultiMap headers ) {
        MultiMap result = new HeadersAdaptor(new DefaultHttpHeaders());
        Set<String> names = headers.names();
        for ( String name : names ) {
            if ( !name.startsWith(Constants.HEADER_DOOR_GOD_PREFIX)) {
                result.add(name, headers.getAll(name));
            }
        }
        return result;
    };


}

