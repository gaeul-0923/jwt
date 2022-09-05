package com.example.demo.common.utill;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * medic_backend
 * CustomPasswordEncoder
 * @author Jayden
 * @since 2021.12.30
 */
public class CustomPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return CryptoUtil.encryptlogin(rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return CryptoUtil.matches(rawPassword.toString(), encodedPassword.toString());
	}
}
