package com.example.demo.jwt;




import com.example.demo.api.auth.AuthService;
import com.example.demo.api.auth.CustomUserDetails;
import com.example.demo.api.auth.LoginVO;
import com.example.demo.common.utill.CommUtil;
import com.example.demo.common.utill.Cookies;
import com.example.demo.common.utill.CryptoUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Token 생성, 검증 클래스
 */
@Log4j2
@Component
@Transactional(rollbackFor = Exception.class)
public class TokenProvider implements InitializingBean {

	private final AuthService authService;

	private static final String AUTHORITIES_KEY = "auth";
	public static final String AUTHORIZATION_HEADER = "Authorization";

	public static final String ACCESS_TOKEN_HEADER = "AccessToken";
	public static final String REFRESH_TOKEN_HEADER = "RefreshToken";

	private final String secret;
	private final Long accessTokenValidMillisecond = 60 * 60 * 1000L; // 1시간
	private final Long refreshTokenValidMillisecond = 24 * 60 * 60 * 1000L; // 1일

	private Key key;

	public TokenProvider(@Value("${jwt.secret}") String secret, AuthService authService) {
		this.secret = secret;
		this.authService = authService;
	}

	// secret값을 base64 decode애서 key변수에 할당
	@Override
	public void afterPropertiesSet() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Authentication 객체의 권한정보를 이용해서 Token 생성
	 * @param authentication
	 * @return jwt token
	 */
	public TokenVO createToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		// Claims 에 user 정보 추가
		//Claims claims = Jwts.claims().setSubject(String.valueOf(authentication.getPrincipal()));
		UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

		// 토큰에서 가지고 다닐 정보 (아이디, 이름, 생년월일, 성별, 연락처)
		HashMap<String, Object> claims = new HashMap<>();
		claims.put("user_name", customUserDetails.getUser_name());

		// qpplication.yml 에서 설정한 토큰의 만료시간 셋팅
		Date now = new Date();

		// accessToken 생성
		String accessToken = Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setSubject(authentication.getName())
			.claim(AUTHORITIES_KEY, authorities)
			.addClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + accessTokenValidMillisecond))
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		// refreshToken 생성
		String refreshToken = Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setExpiration(new Date(now.getTime() + refreshTokenValidMillisecond))
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		// refresh token 저장
		LoginVO loginVO = new LoginVO();
		loginVO.setUser_id(authentication.getName());
		loginVO.setRefresh_token(refreshToken);
		authService.updateRefreshToken(loginVO);

		return TokenVO.builder()
			.grantType("bearer")
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenExpireDate(accessTokenValidMillisecond)
			.build();
	}

	/**
	 * Token의 정보를 이용해 Authentication 객체를 리턴
	 * @param token
	 * @return Authentication
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = Jwts
			.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();

		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		String pid = "";
		if (CommUtil.isNotEmpty(claims.get("pid"))) {
			pid = claims.get("pid").toString();
		}
		CustomUserDetails principal = new CustomUserDetails(claims.getSubject(), "", authorities, claims.get("user_name").toString());

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	/**
	 * 토큰 재발급
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public TokenVO reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//String refreshToken = request.getHeader(REFRESH_TOKEN_HEADER);
		Cookies cookies = new Cookies(request);
		String refreshToken = cookies.getValue(REFRESH_TOKEN_HEADER);

		// RefreshToken 존재여부 확인
		if (CommUtil.isEmpty(refreshToken)) {
			log.error("RefreshToken이 없습니다.");
			return null;
		}

		// 만료된 refresh token 에러
		if (!validateToken(refreshToken)) {
			log.error("RefreshToken이 만료되었습니다.");
			// 토큰초기화
			Cookie accessCookie = Cookies.createCookie("AccessToken", "", "/");
			accessCookie.setMaxAge(0);
			response.addCookie(accessCookie);
			Cookie refreshCookie = Cookies.createCookie("RefreshToken", "", "/");
			refreshCookie.setMaxAge(0);
			response.addCookie(refreshCookie);

			return null;
		}

		//  refreshToken 검증
		LoginVO loginVO = new LoginVO();
		loginVO.setRefresh_token(refreshToken);
		LoginVO refreshVO = authService.selectUserRefreshToken(loginVO);
		if (null == refreshVO || "".equals(refreshVO.getUser_id())) {
			log.error("해당 RefreshToken을 사용하는 사용자가 없습니다.");

			// 토큰초기화
			Cookie accessCookie = Cookies.createCookie("AccessToken", "", "/");
			accessCookie.setMaxAge(0);
			response.addCookie(accessCookie);
			Cookie refreshCookie = Cookies.createCookie("RefreshToken", "", "/");
			refreshCookie.setMaxAge(0);
			response.addCookie(refreshCookie);

			return null;
		}

		// 토큰에서 가지고 다닐 정보 (아이디, 이름, 생년월일, 성별, 연락처)
		HashMap<String, Object> claims = new HashMap<>();
		claims.put("user_name", refreshVO.getUser_name());

		// qpplication.yml 에서 설정한 토큰의 만료시간 셋팅
		Date now = new Date();

		// accessToken 생성
		String accessToken = Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setSubject(refreshVO.getUser_id())
			.claim(AUTHORITIES_KEY, "ROLE_ADMIN")
			.addClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + accessTokenValidMillisecond))
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		// refreshToken 생성
		refreshToken = Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setExpiration(new Date(now.getTime() + refreshTokenValidMillisecond))
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();

		// 세션에 아이디,로그인 저장
		HttpSession session = (HttpSession)request.getSession();
		try {
			session.setAttribute("user_name", CryptoUtil.decrypt(refreshVO.getUser_name()));
			session.setAttribute("user_id", refreshVO.getUser_id());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// refresh token 저장
		loginVO.setUser_id(refreshVO.getUser_id());
		loginVO.setRefresh_token(refreshToken);
		authService.updateRefreshToken(loginVO);

		return TokenVO.builder()
			.grantType("bearer")
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenExpireDate(accessTokenValidMillisecond)
			.build();

	}

	/**
	 * Token의 유효성 검증
	 * @param token
	 * @return boolean
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.error("잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			// 만료된 토큰은 resolveToken 과정에서 리프레시토큰으로 재발급받음
			log.info("만료된 JWT 토큰입니다.");
			return true;
		} catch (UnsupportedJwtException e) {
			log.error("지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.error("JWT 토큰이 잘못되었습니다.");
		}
		return false;
	}

	/**
	 * Request Header 에서 Token정보를 가져옴
	 * @param request
	 * @return token
	 */
	public String resolveToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Cookies cookies = new Cookies(request);
		String jwt = cookies.getValue(ACCESS_TOKEN_HEADER);

		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
			return jwt;
		} catch (ExpiredJwtException e) {
			log.info("만료된 JWT 토큰. refreshToken토큰 확인 후 재발급처리.");
			TokenVO tokenVO = reissue(request, response);
			if (null != tokenVO) {
				// 토큰 설정
				Cookie accessToken = Cookies.createCookie("AccessToken", tokenVO.getAccessToken(), "/");
				response.addCookie(accessToken);
				Cookie refreshToken = Cookies.createCookie("RefreshToken", tokenVO.getRefreshToken(), "/");
				response.addCookie(refreshToken);
				return tokenVO.getAccessToken();
			} else {
				// 토큰초기화
				Cookie accessToken = Cookies.createCookie("AccessToken", "", "/");
				accessToken.setMaxAge(0);
				response.addCookie(accessToken);
				Cookie refreshToken = Cookies.createCookie("RefreshToken", "", "/");
				refreshToken.setMaxAge(0);
				response.addCookie(refreshToken);
				return null;
			}
		} catch (Exception e) {
			log.error("JWT 토큰 오류");
			return null;
		}
	}

	/**
	 * 토큰에서 회원 정보 추출
	 * @author jayden
	 * @since 2022.03.19
	 * @return
	 */
	public String getUserId()  {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			CustomUserDetails springSecurityUser = (CustomUserDetails) authentication.getPrincipal();
			String userId = springSecurityUser.getUsername();
			log.debug("userId =======>" + userId);

			return userId;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
