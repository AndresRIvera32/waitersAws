package com.example.pocwaiters.waitersAws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WaitersAwsApplication implements CommandLineRunner {

	@Autowired
	SecretManagerTest secretManagerTest;

	@Autowired
	CognitoTest cognitoTest;

	@Autowired
	LogsGroupTest logsGroupTest;

	@Autowired
	LambdaTest lambdaTest;

	public static void main(String[] args) {
		SpringApplication.run(WaitersAwsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//secretManagerTest.test();
		//cognitoTest.test();
		//logsGroupTest.test();
		lambdaTest.test();
	}
}
