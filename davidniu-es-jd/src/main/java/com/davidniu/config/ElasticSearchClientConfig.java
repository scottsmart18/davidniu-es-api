package com.davidniu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticSearchClientConfig extends ElasticsearchConfiguration {

    public static final String ELASTIC_SEARCH_CLIENT_DEFAULT_HOST = "localhost";
    public static final int ELASTIC_SEARCH_CLIENT_DEFAULT_PORT = 9200;
    public static final String ELASTIC_SEARCH_CLIENT_DEFAULT_USERNAME = "elastic";
    public static final String ELASTIC_SEARCH_CLIENT_DEFAULT_PASSWORD = "changeme";

    @Override
    @Bean
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
            .connectedTo(ELASTIC_SEARCH_CLIENT_DEFAULT_HOST + ":" + ELASTIC_SEARCH_CLIENT_DEFAULT_PORT)
            .withBasicAuth(ELASTIC_SEARCH_CLIENT_DEFAULT_USERNAME, ELASTIC_SEARCH_CLIENT_DEFAULT_PASSWORD)
            .build();
    }
}
