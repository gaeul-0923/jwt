package com.example.demo.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * lulumedic
 * 코드 정의
 * @author Jayden
 * @since 2022.01.09
 */
@AllArgsConstructor
@Getter
public enum ResponseCode {
	ERROR("ERROR"),
	SUCCESS("SUCCESS"),
	;

	private final String code;
}
