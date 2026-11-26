package com.driveeasy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGatewayFilter.class);
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME, startTime);

        log.info("[GATEWAY-IN] Method: {} | Path: {} | Remote: {}",
                request.getMethod(), request.getURI().getPath(), request.getRemoteAddress());

        // Les en-têtes doivent être posés AVANT le commit de la réponse (sinon les headers
        // sont en lecture seule et l'ajout lève une UnsupportedOperationException).
        exchange.getResponse().beforeCommit(() -> {
            long duration = System.currentTimeMillis() - startTime;
            exchange.getResponse().getHeaders().add("X-DriveEasy-Gateway", "Handled");
            exchange.getResponse().getHeaders().add("X-Response-Time-Ms", String.valueOf(duration));
            return Mono.empty();
        });

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GATEWAY-OUT] Method: {} | Path: {} | Status: {} | Duration: {}ms",
                    request.getMethod(), request.getURI().getPath(),
                    exchange.getResponse().getStatusCode(), duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Priorité élevée pour capturer l'ensemble de la traversée
    }
}
