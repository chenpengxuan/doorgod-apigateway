package com.ymatou.doorgod.apigateway.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by tuwenjie on 2016/9/6.
 */
@RunWith(SpringRunner.class)
@SpringBootTest( )
public class BaseTest {

    {
        System.setProperty("vertx.logger-delegate-factory-class-name",
                io.vertx.core.logging.SLF4JLogDelegateFactory.class.getName());
    }

}
