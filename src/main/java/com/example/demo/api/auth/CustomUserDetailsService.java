package com.example.demo.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * medic_backend
 * CustomUserDetailsService
 * @author Jayden
 * @since 2021.12.30
 */
@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final AuthMapper authMapper;

	@Override
	public UserDetails loadUserByUsername(String user_id) throws UsernameNotFoundException {
		HashMap<String, String> userMap = authMapper.selectUserInfo(user_id);

		Collection<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		roles.add(new SimpleGrantedAuthority("ROLE_USER"));
		return new CustomUserDetails(userMap.get("username"), userMap.get("password"), roles, userMap.get("user_name"));
	}
}
