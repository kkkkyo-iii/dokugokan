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
        
        http.authorizeHttpRequests(authorize -> authorize
        	  
        	    .requestMatchers("/user/**", "/favorite/**", "/voting/**", "/mypage").authenticated()
        	           	 
        	    .requestMatchers(
        	        "/", "/login", "/register", 
        	        "/css/**", "/js/**", "/images/**", "/favicon.ico", 
        	        "/dokugokan", "/movies/**", "/movie/**", "/error"
        	    ).permitAll()
       	    
        	    .anyRequest().authenticated() 
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