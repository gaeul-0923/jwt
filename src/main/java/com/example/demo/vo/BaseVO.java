package com.example.demo.vo;



import com.example.demo.common.utill.CommUtil;
import com.example.demo.common.utill.CryptoUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * medic_admin
 * Default VO
 * @author Jayden
 * @since 2022-01-21
 */
@Getter
@Setter
public class BaseVO {

	private String in_dtm;
	private String in_id;
	private String up_dtm;
	private String up_id;

	private String dateFilter;
	private String dtpstart;
	private String dtpend;

	
	private String searchValue;

	//검색어 암호화 필드
	private String enc_searchValue;

	// 검색조건 관련
	private String searchValueFilter;

	/**
	 * VO변수 암호화
	 * @author jayden
	 * @since 2022.01.21
	 */
	public String CryptoEncrypt(String value) {
		try {
			if(CommUtil.isNotEmpty(value)) {
				return CryptoUtil.encrypt(value);
			}
		} catch (Exception e) {
			// 에러 내역은 던지지 않음
		}
		return value;
	}

	/**
	 * VO변수 복호화
	 * @author jayden
	 * @since 2022.01.21
	 */
	public String CryptoDecrypt(String value) {
		try {
			if(CommUtil.isNotEmpty(value)) {
				return CryptoUtil.decrypt(value);
			}
		} catch (Exception e) {
			// 에러 내역은 던지지 않음
		}
		return value;
	}

	/**
	 * 검색어 암호화
	 * @author hazle
	 * @since 2022.03.17
	 */
	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
		this.enc_searchValue = CryptoEncrypt(searchValue);
	}
}
