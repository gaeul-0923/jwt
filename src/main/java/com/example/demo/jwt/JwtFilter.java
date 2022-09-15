package com.example.demo.jwt;




import com.example.demo.api.auth.AuthService;
import com.example.demo.api.auth.CustomUserDetails;
import com.example.demo.common.utill.Cookies;
import com.example.demo.common.utill.CryptoUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * JWT를 위한 커스텀 필터
 */
@Log4j2
public class JwtFilter extends GenericFilterBean {

	public static final String AUTHORIZATION_HEADER = "Authorization";

	private final TokenProvider tokenProvider;
	
	private final AuthService authService;

	public JwtFilter(TokenProvider tokenProvider, AuthService authService) {
		this.tokenProvider = tokenProvider;
		this.authService = authService;
	}

	/**
	 * JWT Token의 인증정보를 SecurityContext에 저장
	 * @param servletRequest
	 * @param servletResponse
	 * @param filterChain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		String jwt = tokenProvider.resolveToken(httpServletRequest, httpServletResponse);
		String requestURI = httpServletRequest.getRequestURI();


		if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
			Authentication authentication = tokenProvider.getAuthentication(jwt);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
			
			// request log 저장
			//logService.insertRequestLog(httpServletRequest, authentication.getName());
			
			//session에 메뉴정보 존재하는지 여부
			HttpSession session = httpServletRequest.getSession();
			try {
				//세션정보 없으면 다시 넣어준다.
				CustomUserDetails springSecurityUser = (CustomUserDetails) authentication.getPrincipal();
				session.setAttribute("user_id", springSecurityUser.getUsername());
				session.setAttribute("user_name", CryptoUtil.decrypt(springSecurityUser.getUser_name()));
				//session.setAttribute("menuList", authService.selectUserMenuList(param));
				log.debug("token은 살아있고, session은 없는경우 다시 session에 정보를 담는다.");
			} catch (Exception e) {
				e.printStackTrace();
			}

			
		} else {
			log.error("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
			Cookie accessToken = Cookies.createCookie("AccessToken", "", "/");
			accessToken.setMaxAge(0);
			httpServletResponse.addCookie(accessToken);
			
			HttpSession session = (HttpSession)httpServletRequest.getSession();
			session.invalidate();
			log.debug("session 삭제");

			// request log 저장
			//logService.insertRequestLog(httpServletRequest, null);
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

}
