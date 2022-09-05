package com.example.demo.api.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class AuthService {

  private final AuthMapper authMapper;

  public int updateRefreshToken(LoginVO loginVO) {
    return authMapper.updateRefreshToken(loginVO);
  }

  public LoginVO selectUserRefreshToken(LoginVO loginVO) {
    return authMapper.selectUserRefreshToken(loginVO);
  }

}
