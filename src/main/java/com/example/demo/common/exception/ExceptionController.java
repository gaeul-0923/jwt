package com.example.demo.common.exception;

import com.example.demo.common.response.ResponseVO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * medic_backend
 * ExceptionController
 * @author Jayden
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/exception")
public class ExceptionController {

	@RequestMapping(value = "/entrypoint")
	public ResponseVO entrypointException() {
		throw new CustomAuthenticationException("인증정보가 없습니다.");
	}

	@RequestMapping(value = "/accessdenied")
	public ResponseVO accessdeniedException() {
		throw new AccessDeniedException("권한이 없습니다.");
	}
}
