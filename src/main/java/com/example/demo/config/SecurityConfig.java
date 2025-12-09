package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                
                // ▼ 1. [先に] 認証が必要なパスを定義する
                // (例：お気に入り、投票、マイページなど、今後作成するパス)
                .requestMatchers("/user/**", "/favorite/**", "/voting/**").authenticated() 
                
                // ▼ 2. [次に] 認証が不要なパスをすべて許可する
                // (静的リソース、公開ページ、エラーページ)
                .requestMatchers(
                    "/",  "/register", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/dokugokan", "/movies/**", 
                    "/movie/**", 
                    "/error" // エラーページも許可
                ).permitAll()
                
                // ▼ 3. [最後に] 上記以外すべてを定義する
                // .anyRequest().authenticated() // ← これが404を妨げていた
                .anyRequest().permitAll() // ← それ以外（存在しないURL含む）は許可する
                                            // MVCが処理して404を返せるようにする
            )
            .formLogin(login -> login
                .loginPage("/login") 
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/mypage", true) 
                .failureUrl("/login?error")
                .permitAll() 
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login") 
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // パスワードを安全にハッシュ化するための部品
        return new BCryptPasswordEncoder();
    }
}