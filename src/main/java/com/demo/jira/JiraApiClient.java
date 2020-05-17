package com.demo.jira;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Component
public class JiraApiClient {
    @Value("${jira.host}")
    private String jiraHost;

    @Value("${jira.search.api.endpoint}")
    private String jiraSearchAPIEndpoint;

    @Value("${username}")
    private String username;

    @Value("${password}")
    private String password;

    @Autowired
    private RestTemplate restTemplate;

    private HttpHeaders createHeaders(final String username, final String password) {
        return new HttpHeaders() {{
            final String auth = username + ":" + password;
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")));
            final String authHeader = "Basic " + new String(encodedAuth);

            set("Authorization", authHeader);
        }};
    }

    public String search(final String jql, final Integer startAt, final Integer maxResults) {
        return restTemplate.exchange(getCompleteURL(jql, startAt, maxResults),
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(username, password)),
                String.class).getBody();
    }

    private String getCompleteURL(final String jql, final Integer startAt, final Integer maxResults) {
        final StringBuilder searchApiURLBuilder = new StringBuilder(jiraHost);

        searchApiURLBuilder.append(jiraSearchAPIEndpoint);
        searchApiURLBuilder.append(jql);
        searchApiURLBuilder.append("&startAt=");
        searchApiURLBuilder.append(startAt);
        searchApiURLBuilder.append("&maxResults=");
        searchApiURLBuilder.append(maxResults);
        return searchApiURLBuilder.toString();
    }
}
