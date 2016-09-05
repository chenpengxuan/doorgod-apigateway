package com.ymatou.doorgod.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ComponentScan("com.ymatou.doorgod.apigateway")
@ImportResource("classpath:spring/spring-extra-beans.xml")
public class ApigatewayApplication {

	public static void main(String[] args) {
		System.setProperty("vertx.logger-delegate-factory-class-name",
				io.vertx.core.logging.SLF4JLogDelegateFactory.class.getName());
		SpringApplication.run(ApigatewayApplication.class, args);
		for ( ;;) {
			System.out.println("100");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
