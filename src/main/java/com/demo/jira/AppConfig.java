package com.demo.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    private final Environment environment;

    @Autowired
    public AppConfig(final Environment environment) {
        this.environment = environment;
    }

    @Bean
    public String jiraHost() {
        return environment.getProperty("jira.host");
    }

    @Bean
    public String jiraSearchApiEndpoint() {
        return environment.getProperty("jira.search.api.endpoint");
    }

    @Bean
    public String username() {
        return environment.getProperty("username");
    }

    @Bean
    public String password() {
        return environment.getProperty("password");
    }

    @Bean
    public String jql() {
        return environment.getProperty("jql");
    }

    @Bean
    public String outputDir() {
        return environment.getProperty("output.dir");
    }

    @Bean
    public String batchSize() {
        return environment.getProperty("batch.size");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
