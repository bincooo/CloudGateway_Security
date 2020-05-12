package org.example.config;

import org.example.security.*;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationWebFilter;

import java.util.HashMap;

@EnableWebFluxSecurity
@SpringBootConfiguration
public class SecurityConfig
{
    @Bean
    SecurityWebFilterChain securityWebFilter(ServerHttpSecurity http)
    {
        return http.formLogin()
                .authenticationFailureHandler(new JsonFailureHandler())
                .authenticationSuccessHandler(new JsonSuccessHandler())
                .and()
                .exceptionHandling().authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                .and()
                .csrf()
                .disable()
                .authorizeExchange()
                .anyExchange()
                .authenticated()
                .and()
                .addFilterAt(new AuthorizationWebFilter(authorizationManager()), SecurityWebFiltersOrder.AUTHORIZATION)
                //.exceptionHandling().accessDeniedHandler(new JsonAccessDeniedHandler())
                //.and()
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
        attributes.put("/index", "roles[user,admin]");
        attributes.put("/baidu/s", "roles[admin]");
        authorizationManager.uploadMappings(attributes);
        return authorizationManager;
    }
}
