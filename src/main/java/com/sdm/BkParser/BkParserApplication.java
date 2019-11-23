package com.sdm.BkParser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BkParserApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(BkParserApplication.class, args);

		ParserService parserStarterService = context.getBean(ParserService.class);

		parserStarterService.startWorking();


	}

}
