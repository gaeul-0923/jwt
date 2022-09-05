package com.example.demo.common.response;

import lombok.Getter;
import lombok.Setter;

/**
 * tpa
 * ResponseVO
 * @author Jayden
 * @since 2022-01-10
 */
@Getter
@Setter
public class ResponseVO {

	private String result;
	private Object data;

	public ResponseVO(ResponseCode result, Object object) {
		this.data = object;
		this.result = result.getCode();
	}

}
