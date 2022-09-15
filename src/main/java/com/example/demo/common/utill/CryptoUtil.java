/*
 * Copyright  (c) 2013.
 */

package com.lkms.tpa.common.utill;

import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * MD5 SHA256 AES 암호화 관련 처리
 *
 * @author Jayden
 * @since 2021.12.17
 */
@Log4j2
@Component(value = "CryptoUtil")
public class CryptoUtil {

	private static final String salt = "95426e99986f6e6865616c6874696e67";
	private static final String sha_salt = "(Kgnrxkz99Rqn7JLFi7RALPBu7lz4mym";

	private static String iv;
	private static Key keySpec;
	private final static String key = salt;

	public static String getIv() {
		return iv;
	}

	public static void setIv(String iv) {
		CryptoUtil.iv = iv;
	}

	public static Key getKeySpec() {
		return keySpec;
	}

	public static void setKeySpec(Key keySpec) {
		CryptoUtil.keySpec = keySpec;
	}

	/**
	 * 16자리의 키값을 입력하여 객체를 생성한다.
	 *
	 * @throws UnsupportedEncodingException 키값의 길이가 16이하일 경우 발생
	 */
	public static void AES256Util() throws UnsupportedEncodingException {

		//this.iv = key.substring(0, 16);
		setIv("lkmskey2021?^34$");
		byte[] keyBytes = new byte[16];
		byte[] b = key.getBytes(StandardCharsets.UTF_8);
		int len = b.length;
		if (len > keyBytes.length) {
			len = keyBytes.length;
		}
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		//this.keySpec = keySpec;
		setKeySpec(keySpec);
	}

	/**
	 * AES256 으로 암호화 한다.
	 *
	 * @param str 암호화할 문자열
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws GeneralSecurityException
	 * @throws UnsupportedEncodingException
	 */
	public static String encrypt(String str)
		throws NoSuchAlgorithmException, GeneralSecurityException, UnsupportedEncodingException {
		AES256Util();
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
		String enStr = new String(Base64.encodeBase64(encrypted));
		return enStr;
	}

	/**
	 * AES256으로 암호화된 txt 를 복호화한다.
	 *
	 * @param str 복호화할 문자열
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws GeneralSecurityException
	 * @throws UnsupportedEncodingException
	 */
	public static String decrypt(String str)
		throws NoSuchAlgorithmException, GeneralSecurityException, UnsupportedEncodingException {
		AES256Util();
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] byteStr = Base64.decodeBase64(str.getBytes());
		return new String(c.doFinal(byteStr), StandardCharsets.UTF_8);
	}

	/**
	 * 로그인 페스워드 암호화  by 스프링 시큐리티
	 *
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static String encryptlogin(String message) {
		MessageDigest md = null;
		String enc = "";
		try {
			md = MessageDigest.getInstance("SHA-512");
			md.reset();
			md.update((message + sha_salt).getBytes(StandardCharsets.UTF_8));
			enc = String.format("%0128x", new BigInteger(1, md.digest()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return enc;
	}

	/**
	 * 로그인 페스워드 암호화  비교
	 *
	 * @param rawPassword     (암호화 되지 않은 페스워드)
	 * @param encodedPassword (암호화 된 페스워드)
	 * @return boolean
	 */
	public static boolean matches(String rawPassword, String encodedPassword) {
		boolean result = false;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.reset();
			md.update((rawPassword + sha_salt).getBytes(StandardCharsets.UTF_8));
			String enc = String.format("%0128x", new BigInteger(1, md.digest()));
			result = enc.equals(encodedPassword);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return result;
	}

	public static String XmlEncrypt(String encryptkey, Map<String, String> input_data) {
		try {
			if (null != encryptkey) {
				StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
				encryptor.setPassword(encryptkey);

				if (null != input_data) {
					for (String mapkey : input_data.keySet()) {
						log.info("key:" + mapkey);
						log.info("value:" + input_data.get(mapkey));
						log.info("encrypt:" + encryptor.encrypt(input_data.get(mapkey)));
					}
				} else {
					return "암호화 대상 데이터가 없습니다.";
				}

			} else {
				return "암호화키를 입력하세요";
			}
		} catch (Exception e) {
			return "오류 발생";
		}
		return "암호화 성공";
	}

	public static String XmlDecrypt(String decryptkey, Map<String, String> input_data) {
		try {
			if (null != decryptkey) {
				StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
				encryptor.setPassword(decryptkey);

				if (null != input_data) {
					for (String mapkey : input_data.keySet()) {
						log.info("key:" + mapkey);
						log.info("value:" + input_data.get(mapkey));
						log.info("encrypt:" + encryptor.decrypt(input_data.get(mapkey)));
					}
				} else {
					return "복호화 대상 데이터가 없습니다.";
				}
			} else {
				return "복호화키를 입력하세요";
			}
		} catch (Exception e) {
			return "오류 발생";
		}
		return "복호화 성공";
	}
}
