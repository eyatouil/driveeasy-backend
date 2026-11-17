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

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long start = exchange.getAttribute(START_TIME);
            long duration = (start != null) ? (System.currentTimeMillis() - start) : 0L;
            var response = exchange.getResponse();

            response.getHeaders().add("X-DriveEasy-Gateway", "Handled");
            response.getHeaders().add("X-Response-Time-Ms", String.valueOf(duration));

            log.info("[GATEWAY-OUT] Method: {} | Path: {} | Status: {} | Duration: {}ms",
                    request.getMethod(), request.getURI().getPath(), response.getStatusCode(), duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Priorité élevée pour capturer l'ensemble de la traversée
    }
}
