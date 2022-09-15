package com.example.demo.api.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * medic_backend
 *
 * @author Jayden
 * @since 2021.12.30
 */
@Getter
@Setter
public class CustomUserDetails extends User {

	private String user_name;
	private String auth;
	private String login_yn;
	private int enabled;
	private String pid;
	private String temp_pw_yn;

	public CustomUserDetails(String username, String password,
													 Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}

	public CustomUserDetails(String username, String password,
													 Collection<? extends GrantedAuthority> authorities, String user_name) {
		super(username, password, authorities);
		this.user_name = user_name;
	}
}
