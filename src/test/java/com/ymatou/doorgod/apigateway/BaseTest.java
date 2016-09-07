package com.ymatou.doorgod.apigateway;

import io.vertx.core.Vertx;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by tuwenjie on 2016/9/6.
 */
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "com.ymatou.doorgod.apigateway",excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApigatewayApplication.class),
})
@ImportResource("classpath:spring/spring-extra-beans.xml")
@ContextConfiguration(classes = BaseTest.class)
public class BaseTest {

    {
        System.setProperty("vertx.logger-delegate-factory-class-name",
                io.vertx.core.logging.SLF4JLogDelegateFactory.class.getName());
    }

    public static Vertx vertx = Vertx.vertx();



}
