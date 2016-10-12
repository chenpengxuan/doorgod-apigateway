package com.ymatou.doorgod.apigateway.test;

import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.TargetServer;
import com.ymatou.doorgod.apigateway.reverseproxy.VertxVerticleDeployer;
import mockit.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
public class ApigatewayApplicationTests extends BaseTest {

	@Autowired
	private MySqlClient mySqlClient;

	@Autowired
	private VertxVerticleDeployer vertxVerticleDeployer;

	@Before
	public void init( ) throws Exception  {
		new Expectations(mySqlClient) {
			{
				mySqlClient.locateTargetServer();
				TargetServer server = new TargetServer();
				server.setHost("localhost");
				server.setPort(8088);
				result = server;

			}
		};



	}

	@Test
	public void contextLoads() throws Exception {
		vertxVerticleDeployer.deployVerticles();
		System.in.read();
	}

}
