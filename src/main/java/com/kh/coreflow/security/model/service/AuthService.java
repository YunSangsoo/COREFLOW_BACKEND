package com.kh.coreflow.security.model.service;

import java.util.Optional;

import com.kh.coreflow.model.dto.UserDto.AuthResult;
import com.kh.coreflow.model.dto.UserDto.User;

public interface AuthService {

	boolean existsByEmail(String email);

	AuthResult login(String email, String userPwd);

	AuthResult signUp(String email, String userPwd);

	AuthResult refreshByCookie(String refreshCookie);

	Optional<User> findUserByUserNo(Long userNo);

	boolean findUserPwd(String userName, String email);

	Optional<User> findUserByEmail(String email);

	String createTempPwd();

	
	
}
