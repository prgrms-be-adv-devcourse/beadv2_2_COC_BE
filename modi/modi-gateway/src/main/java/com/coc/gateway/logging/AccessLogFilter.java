package com.coc.gateway.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startMs = System.currentTimeMillis();
        return chain.filter(exchange).doFinally(signalType -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            long durationMs = System.currentTimeMillis() - startMs;
            String clientIp = getClientIp(request);
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            Integer status = response.getStatusCode() != null ? response.getStatusCode().value() : null;

            if (shouldSkipAccessLog(request.getURI().getPath(), status)) {
                return;
            }
			
			request.getMethod();
			log.info("access_log",
                    kv("event", "access_log"),
                    kv("method", request.getMethod().name()),
                    kv("path", request.getURI().getPath()),
                    kv("query", request.getURI().getRawQuery()),
                    kv("status", status),
                    kv("duration_ms", durationMs),
                    kv("client_ip", clientIp),
                    kv("user_agent", request.getHeaders().getFirst("User-Agent")),
                    kv("request_id", request.getId()),
                    kv("trace_id", firstHeader(request, "X-B3-TraceId", "traceparent")),
                    kv("route_id", route != null ? route.getId() : null),
                    kv("log_type", "access")
            );
        });
    }

    private static String getClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : null;
    }

    private static String firstHeader(ServerHttpRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeaders().getFirst(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static boolean shouldSkipAccessLog(String path, Integer status) {
        if (path == null) {
            return false;
        }
        String normalized = path.toLowerCase();
        boolean isHealth = normalized.contains("/health")
                || normalized.contains("/actuator/health")
                || normalized.contains("/ready")
                || normalized.contains("/live");
        return isHealth && status != null && status == 200;
    }
}
