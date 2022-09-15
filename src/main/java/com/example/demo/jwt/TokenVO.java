package com.example.demo.jwt;

import lombok.*;

/**
 * medic_backend
 * JWT Token VO
 * @author Jayden
 * @since 2021.12.30
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO {
	private String grantType;
	private String accessToken;
	private String refreshToken;
	private Long accessTokenExpireDate;
}
