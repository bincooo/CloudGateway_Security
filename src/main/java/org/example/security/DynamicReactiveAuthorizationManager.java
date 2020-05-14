package org.example.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicReactiveAuthorizationManager implements ReactiveAuthorizationManager<ServerWebExchange>
{
    private final String prefix;
    private final String suffix;

    public DynamicReactiveAuthorizationManager()
    {
        this("[", "]");
    }

    public DynamicReactiveAuthorizationManager(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    private final List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> mappings = new ArrayList<>();

    @Override
    public Mono<Void> verify(Mono<Authentication> authentication, ServerWebExchange exchange)
    {
        return check(authentication, exchange).flatMap(d -> Mono.empty());
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, ServerWebExchange exchange)
    {
        return Flux.fromIterable(this.mappings)
                .concatMap((mapping) -> mapping.getMatcher().matches(exchange).filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                        .map(ServerWebExchangeMatcher.MatchResult::getVariables)
                        .flatMap((variables) -> (mapping.getEntry()).check(authentication, new AuthorizationContext(exchange, variables))
                                .filter(AuthorizationDecision::isGranted).switchIfEmpty(Mono.defer(() -> Mono.error(new AccessDeniedException("Access Denied"))))
                        ))
                .next().defaultIfEmpty(new AuthorizationDecision(false));
    }

    public void uploadMappings(Map<String, String> attributes)
    {
        synchronized (this.mappings)
        {
            this.mappings.clear();
            for (Map.Entry<String, String> entry : attributes.entrySet())
            {
                ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.pathMatchers(entry.getKey());
                String val = entry.getValue();
                ReactiveAuthorizationManager<AuthorizationContext> manager = null;
                if (val.startsWith("roles" + prefix) && val.endsWith(suffix))
                {
                    manager = AuthorityReactiveAuthorizationManager.hasAnyRole(parseAuthorization(val, "roles"));
                }
                else if (val.startsWith("auths" + prefix) && val.endsWith(suffix))
                {
                    manager = AuthorityReactiveAuthorizationManager.hasAnyAuthority(parseAuthorization(val, "auths"));
                }
                if (manager != null)
                {
                    this.mappings.add(new ServerWebExchangeMatcherEntry<>(matcher, manager));
                }
            }
        }
    }

    private String[] parseAuthorization(String authorization, String type)
    {
        authorization = authorization.substring(type.length() + prefix.length());
        return authorization.substring(0, authorization.length() - suffix.length()).split("\\s*,\\s*");
    }
}
