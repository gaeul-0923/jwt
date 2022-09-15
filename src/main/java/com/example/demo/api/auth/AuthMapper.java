package com.example.demo.api.auth;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
@Mapper
public interface AuthMapper {
  HashMap<String, String> selectUserInfo(String user_id) throws UsernameNotFoundException;

  int updateRefreshToken(LoginVO loginVO);

  LoginVO selectUserRefreshToken(LoginVO loginVO);

}
