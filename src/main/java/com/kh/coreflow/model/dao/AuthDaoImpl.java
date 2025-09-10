package com.kh.coreflow.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import com.kh.coreflow.model.dto.UserDto.User;
import com.kh.coreflow.model.dto.UserDto.UserAuthority;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuthDaoImpl implements AuthDao{
	
	private final SqlSessionTemplate session;
	
	@Override
	public Optional<User> findUserByEmail(String email) {
		User user = session.selectOne("auth.findUserByEmail" , email);
		Optional<User> optionalUser = Optional.ofNullable(user);
		return optionalUser;
	}

	@Override
	public void insertUser(User user) {
		session.insert("auth.insertUser",user);
	}

	@Override
	public void insertUserRole(UserAuthority auth) {
		session.insert("auth.insertUserRole",auth);
	}

	@Override
	public Optional<User> findUserByUserNo(Long userNo) {
		User user = session.selectOne("auth.findUserByUserNo" , userNo);
		Optional<User> optionalUser = Optional.ofNullable(user);
		return optionalUser;
	}

	@Override
	public User findUserPwd(String name, String email) {
		Map<String, String> param = new HashMap<>();
		param.put("name", name);
		param.put("email", email);
		return session.selectOne("auth.findUserPwd", param);
	}

	@Override
	public void updatePwd(String email, String encodedPwd) {
		Map<String, String> param = new HashMap<>();
		param.put("email", email);
		param.put("encodedPwd", encodedPwd);
		session.update("auth.updatePwd", param);
	}

	@Override
	public int checkProfileImage(Long userNo) {
		return session.selectOne("auth.checkProfileImage", userNo);
	}
	
	@Override
	public void insertProfileImage(Map<String, Object> imageUpdate) {
		session.insert("auth.insertProfileImage", imageUpdate);
	}
	
	@Override
	public void updateProfileImage(Map<String, Object> imageUpdate) {
		session.update("auth.updateProfileImage", imageUpdate);
	}

	@Override
	public void updatePhone(Long userNo, String string) {
		Map<String, Object> param = new HashMap<>();
		param.put("userNo", userNo);
		param.put("string", string);
		session.update("auth.updatePhone", param);
	}

	@Override
	public void updateAddress(Long userNo, String string) {
		Map<String, Object> param = new HashMap<>();
		param.put("userNo", userNo);
		param.put("string", string);
		session.update("auth.updateAddress", param);
	}

	@Override
	public UserAuthority findUserAuthorityByUserNo(Long userNo) {
	    List<String> roles = session.selectList("auth.findUserAuthorityByUserNo", userNo);
	    if (roles.isEmpty()) return null;

	    return UserAuthority.builder()
	            .userNo(userNo)
	            .roles(roles)
	            .build();
	}
	
	
}
