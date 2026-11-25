package com.driveeasy.location.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    /**
     * RestTemplate load-balancé (résolution des noms via Eureka).
     * On utilise le client HTTP du JDK (et non HttpURLConnection) car ce dernier
     * ne supporte pas la méthode PATCH, nécessaire pour les appels
     * PATCH /vehicules/{id}/statut et PATCH /clients/{id}/categorie.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));
        // Construit via RestTemplateBuilder : active l'observation Micrometer, donc la
        // propagation du traceId vers les autres services (traces Zipkin multi-services).
        return builder.requestFactory(() -> factory).build();
    }
}
