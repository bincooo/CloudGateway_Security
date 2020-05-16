package org.example.security;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class JsonAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint
{
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e)
    {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String val = "";
        try
        {
            val = objectMapper.writeValueAsString(new Entity<>(401, "未登陆无法访问"));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
//        DataBuffer buffer = response.bufferFactory().wrap(JSONObject.toJSONString(new Entity<>(401, "未登陆无法访问")).getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer = response.bufferFactory().wrap(val.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
