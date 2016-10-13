package com.ymatou.doorgod.apigateway.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by tuwenjie on 2016/9/13.
 */
@Configuration
public class BeanConfig {

    @Value("${tomcatPort}")
    private int tomcatPort;

    @Bean
    public Vertx vertx( ) {

        //当前，无需更多特殊配置
        VertxOptions vertxOptions = new VertxOptions();
        return Vertx.vertx(vertxOptions);
    }


//    @Bean
//    public ServletRegistrationBean servletRegistrationBean( ) {
//        return new ServletRegistrationBean(new HystrixMetricsStreamServlet(),"/hystrix.stream");
//    }


    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return (container -> {
            container.setPort(tomcatPort);
        });
    }


}
