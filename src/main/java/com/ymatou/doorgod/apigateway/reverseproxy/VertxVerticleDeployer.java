package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.SpringContextHolder;
import com.ymatou.doorgod.apigateway.cache.HystrixConfigCache;
import com.ymatou.doorgod.apigateway.cache.UriConfigCache;
import com.ymatou.doorgod.apigateway.config.AppConfig;
import com.ymatou.doorgod.apigateway.integration.KafkaClient;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.TargetServer;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.DimensionKeyValueFetcher;
import com.ymatou.doorgod.apigateway.reverseproxy.filter.FiltersExecutor;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 在Spring Application context启动完毕后，部署Vertx verticles
 * Created by tuwenjie on 2016/9/21.
 */
@Component
public class VertxVerticleDeployer {

    //vertice实例个数
    public static final int VERTICLE_INSTANCES = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;

    public static final String ADDRESS_START_PRE_CONNECT_TARGET_SERVER = "address-start-connect";

    public static final String ADDRESS_END_ONE_PRE_CONNECTION = "address-end-one-connection";

    public static final String ADDRESS_END_BIND = "address-end-bind";

    public static final String SUCCESS_MSG = "ok";

    public static TargetServer targetServer = null;

    public static Vertx vertx = null;

    public static volatile boolean startUpSuccess = false;

    /**
     * 将非Spring托管的类(主要是vert.x的{@link HttpServerRequestHandler}, {@link HttpServerVerticle}等)需要用到的Spring bean注册到这里
     * 避免这些类频繁重复调用{@link com.ymatou.doorgod.apigateway.SpringContextHolder#getBean(Class)}
     */
    public static HystrixConfigCache hystrixConfigCache;
    public static FiltersExecutor filtersExecutor;
    public static DimensionKeyValueFetcher dimensionKeyValueFetcher;
    public static AppConfig appConfig;
    public static KafkaClient kafkaClient;
    public static UriConfigCache uriConfigCache;




    @Autowired
    private MySqlClient mySqlClient;


    private static Logger LOGGER = LoggerFactory.getLogger(VertxVerticleDeployer.class);

    public void deployVerticles() throws Exception {

        registerBeans();

        //当前无需更多配置
        VertxOptions vertxOptions = new VertxOptions();
        vertx = Vertx.vertx(vertxOptions);

        targetServer = mySqlClient.locateTargetServer();

        //监听Vertx http server端口绑定完毕
        AtomicReference<Exception> bindException = new AtomicReference<>();
        CountDownLatch bindLatch = new CountDownLatch(1);
        vertx.eventBus().consumer(VertxVerticleDeployer.ADDRESS_END_BIND, event -> {
            if ( !SUCCESS_MSG.equals(event.body().toString())) {
                bindException.set(new Exception(event.body().toString()));
            }
            bindLatch.countDown();
        });

        //监听Vertx verticles启动完毕
        CountDownLatch deployLatch = new CountDownLatch(1);
        AtomicReference<Throwable> deployException = new AtomicReference<>();

        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(VERTICLE_INSTANCES),
                result -> {
                    if (result.failed()) {
                        deployException.set(result.cause());
                    }
                    deployLatch.countDown();
                });

        //等待Verticles部署完成
        deployLatch.await();


        if (deployException.get() != null) {
            throw new RuntimeException("Failed to deploy vertx verticles", deployException.get());
        }


        //等待http server端口bind完毕
        bindLatch.await();

        if ( bindException.get() != null ) {
            throw bindException.get();
        }


        if ( StringUtils.hasText(appConfig.getTargetServerWarmupUri())) {
            boolean[] warmUpSuccess = new boolean[]{false};

            CountDownLatch createConnsLatch = new CountDownLatch(VERTICLE_INSTANCES * appConfig.getMaxHttpConnectionPoolSize());

            vertx.eventBus().consumer(ADDRESS_END_ONE_PRE_CONNECTION, event -> {
                if (event.body().toString().equals(SUCCESS_MSG)) {
                    //有一个连接建立成功，即表示warmup成功
                    warmUpSuccess[0] = true;
                }
                createConnsLatch.countDown();
            });

            LOGGER.info("Creating {} connections to target server:{} in advance.", createConnsLatch.getCount(), targetServer);

            //通知各个Verticle去预创建到TargetServer的连接
            vertx.eventBus().publish(ADDRESS_START_PRE_CONNECT_TARGET_SERVER, "");

            //等待各个Verticle预创建连接完毕
            createConnsLatch.await();

            LOGGER.info("Finished in creating connections to target server.");

            if (!warmUpSuccess[0]) {
                throw new RuntimeException("Failed to startup ApiGateway because warmming up target server failed.");
            }

        } else {
            LOGGER.warn("Targe server warmup url not set");
        }
        startUpSuccess = true;
        LOGGER.info("Succeed in startup ApiGateway. CPU cores:{}, Event loop thread count:{}",
                Runtime.getRuntime().availableProcessors(),
                VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE);
    }

    @PreDestroy
    public void destroy() {
        vertx.close();
    }

    /**
     * 供非Spring托管实例使用Spring bean
     */
    private void registerBeans( ) {
        hystrixConfigCache = SpringContextHolder.getBean(HystrixConfigCache.class);
        filtersExecutor = SpringContextHolder.getBean(FiltersExecutor.class);
        dimensionKeyValueFetcher = SpringContextHolder.getBean(DimensionKeyValueFetcher.class);
        appConfig = SpringContextHolder.getBean(AppConfig.class);
        kafkaClient = SpringContextHolder.getBean(KafkaClient.class);
        uriConfigCache = SpringContextHolder.getBean(UriConfigCache.class);
    }

}