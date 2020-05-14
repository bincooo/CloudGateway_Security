package org.example.security;

import org.springframework.security.core.userdetails.*;
import reactor.core.publisher.Mono;

public class DynamicAuthenticationUserService implements ReactiveUserDetailsService
{
    /**
     * 用户名称检验
     *
     * @param username 查找用户信息
     * @return 返回用户信息
     */
    @Override
    public Mono<UserDetails> findByUsername(String username)
    {
        // 假装查询了数据库 密码都是 `password`
        if ("user".equals(username))
        {
            return Mono.create(sink -> sink.success(User.builder()
                    .username("user")
                    .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                    .roles("user")
                    .authorities("query")
                    .build())
            );
        }
        if ("admin".equals(username))
        {
            return Mono.create(sink -> sink.success(User.builder()
                    .username("admin")
                    .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                    .roles("user", "admin")
                    .authorities("query", "update", "delete")
                    .build())
            );
        }
        return Mono.error(new UsernameNotFoundException("未知的用户"));
    }
}