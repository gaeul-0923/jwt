package com.example.demo.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * medic_backend
 *
 * @author Jayden
 * @since 2022-01-17
 */
public class CustomAuthenticationException extends AuthenticationException {

	public CustomAuthenticationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public CustomAuthenticationException(String msg) {
		super(msg);
	}
}
