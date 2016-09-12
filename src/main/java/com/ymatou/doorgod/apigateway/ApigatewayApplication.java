package com.ymatou.doorgod.apigateway;

import com.ymatou.doorgod.apigateway.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan("com.ymatou.doorgod.apigateway")
@ImportResource("classpath:spring/spring-extra-beans.xml")
public class ApigatewayApplication {

	public static void main(String[] args) {
		System.setProperty("vertx.logger-delegate-factory-class-name",
				io.vertx.core.logging.SLF4JLogDelegateFactory.class.getName());
		ConfigurableApplicationContext springContext = SpringApplication.run(ApigatewayApplication.class, args);

		deployVerticles();
	}

	private static void deployVerticles( ) {
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(HttpServerVerticle.class.getName(),
				new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE));
	}
}
