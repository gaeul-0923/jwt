package com.example.demo.api.auth;



import com.example.demo.common.response.ResponseCode;
import com.example.demo.common.response.ResponseVO;
import com.example.demo.common.utill.Cookies;
import com.example.demo.common.utill.CryptoUtil;
import com.example.demo.jwt.TokenProvider;
import com.example.demo.jwt.TokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
  private final TokenProvider tokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  @Autowired
  private AuthService authService;

  public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
    this.tokenProvider = tokenProvider;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
  }

  @PostMapping("/auth/authenticate")
  public ResponseVO authorize(HttpServletRequest request, @Valid @RequestBody LoginVO loginVO, HttpServletResponse response) throws Exception {
    return doLogin(loginVO, request, response);
  }

  private ResponseVO doLogin(LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
    response.setHeader("Access-Control-Allow-Credentials","true");

    // loginDto의 userid와 password를 이용해 UsernamePasswordAuthenticationToken 객체생성
    UsernamePasswordAuthenticationToken authenticationToken =
      new UsernamePasswordAuthenticationToken(loginVO.getUser_id(), loginVO.getLogin_pw());

    // authenticate 메소드가 실행될때 CustomUserDetailsService.loadUserByUsername 메소드가 실행됨
    Authentication authentication = null;
    try {
      authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    } catch (Exception e) {
      e.printStackTrace();
      // 로그인 로그저장
      //logService.insertLoginLog(request, loginVO.getUser_id(), "LOGIN", "FAIL");
      throw e;
    }

    // 임시 비밀번호 로그인
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    if ("Y".equals(userDetails.getTemp_pw_yn())) {
      // 로그인 로그저장
      //logService.insertLoginLog(request, loginVO.getUser_id(), "LOGIN", "FAIL");
      Map<String, String> errorMap = new HashMap<String, String>();
      errorMap.put("msg", loginVO.getUser_id());
      return new ResponseVO(ResponseCode.ERROR, errorMap);
    }

    // authentication 객체를 SecurityContext에 저장
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // JWT Token 생성
    TokenVO tokenVO = tokenProvider.createToken(authentication);

    // 로그인 로그저장
    //logService.insertLoginLog(request, loginVO.getUser_id(), "LOGIN", "SUCCESS");

    // 토큰은 쿠키에 저장 (secure, httponly 설정)
    Cookie accessToken = Cookies.createCookie("AccessToken", tokenVO.getAccessToken(), "/");
    response.addCookie(accessToken);
    Cookie refreshToken = Cookies.createCookie("RefreshToken", tokenVO.getRefreshToken(), "/");
    response.addCookie(refreshToken);

    // 저장후 refreshToken은 response 하지 않음
    tokenVO.setAccessToken("");
    tokenVO.setRefreshToken("");

    //메뉴세션...
    //session에 메뉴정보 존재하는지 여부
    HttpSession session = (HttpSession)request.getSession();
    /*if(CommUtil.isEmpty(session.getAttribute("menuList"))) {
      HashMap<String, Object> param = new HashMap<>();
      param.put("user_id", authentication.getName());

      try {
        session.setAttribute("menuList", authService.selectUserMenuList(param));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }*/


    // 세션에 아이디,로그인 저장
    CustomUserDetails springSecurityUser = (CustomUserDetails) authentication.getPrincipal();
    session.setAttribute("user_id", springSecurityUser.getUsername());
    session.setAttribute("user_name", CryptoUtil.decrypt(springSecurityUser.getUser_name()));

    return new ResponseVO(ResponseCode.SUCCESS, tokenVO);
  }
}
