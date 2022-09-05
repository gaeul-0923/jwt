package com.example.demo.api.auth;

import com.example.demo.vo.BaseVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * medic_backend
 *
 * @author Jayden
 * @since 2021.12.30
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class LoginVO extends BaseVO {

	private String user_id;
	private String login_id;
	private String login_nm;
	private String login_pw;
	private String crypto_login_pw;

	private String login_type;
	private String login_dtm;
	private String wrong_count;
	private String login_yn;
	private String pwd_up_dtm;
	private String pwd_up_id;
	private String refresh_token;
	private String user_name;
	
	private String pid;

	public LoginVO(String user_id, String login_pw) {
		this.user_id = user_id;
		this.login_pw = login_pw;
	}
	
	public String getLogin_pw() {
		return CryptoDecrypt(login_pw);
	}

	public void setLogin_pw(String login_pw) {
		this.login_pw = login_pw;
		this.crypto_login_pw = CryptoEncrypt(login_pw);
	}
}
