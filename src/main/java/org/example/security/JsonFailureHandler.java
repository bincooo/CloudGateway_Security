package org.example.security;

import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class JsonFailureHandler implements ServerAuthenticationFailureHandler
{
    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange filterExchange, AuthenticationException e)
    {
        ServerHttpResponse response = filterExchange.getExchange().getResponse();
        if (e instanceof UsernameNotFoundException)
        {
            return doWriter(response, "未知的用户");
        }
        if (e instanceof BadCredentialsException)
        {
            return doWriter(response, "密码不正确");
        }
        if (e instanceof LockedException)
        {
            return doWriter(response, "用户已锁定");
        }
        else
            return doWriter(response, "系统错误");
    }

    private Mono<Void> doWriter(ServerHttpResponse response, String msg)
    {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(JSONObject.toJSONString(new Entity<>(401, msg)).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}