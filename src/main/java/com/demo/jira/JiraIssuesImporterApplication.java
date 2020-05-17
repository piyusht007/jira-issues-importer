package com.demo.jira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JiraIssuesImporterApplication {
    public static void main(String[] args) {
        final ConfigurableApplicationContext ctx = new SpringApplicationBuilder(JiraIssuesImporterApplication.class) //
                .web(WebApplicationType.NONE) //
                .run(args);

		final int exitCode = SpringApplication.exit(ctx, () -> 0);
		System.exit(exitCode);
	}
}
