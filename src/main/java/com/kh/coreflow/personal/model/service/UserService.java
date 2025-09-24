package com.kh.coreflow.personal.model.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.model.dto.UserDto.UserUpdate;

public interface UserService {

	List<Object> getMySchedule(Long userNo);

	void updateMyInfo(UserUpdate userUpdate, MultipartFile profile);

	void updatePassword(Long userNo, String userPwd, String string);

	void updatePhone(Long userNo, String string);

	void updateAddress(Long userNo, String string, String string2);
	
}
