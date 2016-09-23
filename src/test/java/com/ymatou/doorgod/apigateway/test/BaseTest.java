package com.ymatou.doorgod.apigateway.test;

import com.ymatou.doorgod.apigateway.ApigatewayApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

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


}
