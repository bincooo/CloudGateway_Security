package org.example.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.security.*;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.util.HashMap;
import java.util.List;

@EnableWebFluxSecurity
@SpringBootConfiguration
public class SecurityConfig
{
    @Bean
    SecurityWebFilterChain securityWebFilter(ServerHttpSecurity http)
    {
        return http
                .authorizeExchange()
                .pathMatchers("/actuator", "favicon.ico")
                .permitAll()
                .anyExchange()
                .authenticated()
                .and()
                .formLogin()
                .authenticationFailureHandler(new JsonFailureHandler())
                .authenticationSuccessHandler(new JsonSuccessHandler())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                .and()
                .csrf()
                .disable()
                .addFilterAt(new AuthorizationWebFilter(authorizationManager()), SecurityWebFiltersOrder.AUTHORIZATION)
                .exceptionHandling().accessDeniedHandler(new JsonAccessDeniedHandler())
                .and()
                .build();
    }


    @Bean
    public ReactiveUserDetailsService userDetailsService()
    {
        return new DynamicAuthenticationUserService();
    }


    @Bean
    public DynamicReactiveAuthorizationManager authorizationManager()
    {
        DynamicReactiveAuthorizationManager authorizationManager = new DynamicReactiveAuthorizationManager();
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("/index", "auths[query]");
        attributes.put("/baidu/s", "auths[update]");
        authorizationManager.uploadMappings(attributes);
        return authorizationManager;
    }

    @Bean("springSessionDefaultRedisSerializer")
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer()
    {
        Jackson2JsonRedisSerializer<Object> objectJackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        List<Module> modules = SecurityJackson2Modules.getModules(getClass().getClassLoader());
        objectMapper.registerModules(modules);
        objectJackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return objectJackson2JsonRedisSerializer;
    }
}
