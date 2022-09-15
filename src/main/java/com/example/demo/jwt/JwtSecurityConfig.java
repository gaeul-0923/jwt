package com.example.demo.jwt;

import com.example.demo.api.auth.AuthService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * TokenProvider, JwtFilter를 SecurityConfig에 적용시 사용할 클래스
 */
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	private TokenProvider tokenProvider;

	private AuthService authService;

	public JwtSecurityConfig(TokenProvider tokenProvider, AuthService authService) {
		this.tokenProvider = tokenProvider;
		this.authService = authService;
	}

	@Override
	public void configure(HttpSecurity http) {
		JwtFilter customFilter = new JwtFilter(tokenProvider, authService);
		http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
