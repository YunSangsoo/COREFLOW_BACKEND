package com.kh.coreflow.validator;

import org.springframework.stereotype.Component;

import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPost;
import com.kh.coreflow.model.dao.AuthDao;
import com.kh.coreflow.model.dto.UserDto.User;

//유효성 검사
@Component
public class UserValidator {
	
	private final AuthDao dao;
	
	public UserValidator(AuthDao dao) {
		this.dao = dao;
	}

	public ValidationResult validateStep1(MemberPost req) { // 사원 생성용
		if(req == null) return ValidationResult.fail("요청 데이터가 비어 있습니다.");
		
		if(!isValidEmail(req.getEmail())) 
			return ValidationResult.fail("이메일 형식이 잘못되었습니다.");
		if(!isValidPhone(req.getPhone())) 
			return ValidationResult.fail("전화번호 형식이 잘못되었습니다.");
		if(req.getUserName() == null) 
			return ValidationResult.fail("이름을 입력하세요.");
		if(!isValidExtension(req.getExtension())) 
			return ValidationResult.fail("내선 번호가 올바르지 않습니다.");
        
        return ValidationResult.ok();
	}
	
	public ValidationResult validateStep2(User user) { // 내 정보 수정용
		if(!isValidPassword(user.getUserPwd())) 
			return ValidationResult.fail("비밀번호를 영문 대소문자와 숫자 포함 8-15자로 입력하세요.");
		if(!isValidPhone(user.getPhone())) 
			return ValidationResult.fail("전화번호 형식이 잘못되었습니다.");
		
		return ValidationResult.ok();
	}

	private boolean isValidEmail(String email) {
		if(email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,6}$")) {
			if(dao.isEmailExists(email)) return true;
		}
		return true;
	}
	
	private boolean isValidPhone(String phone) {
		return phone != null && phone.matches("^010-\\d{4}-\\d{4}$");
	}
	
	private boolean isValidPassword(String userPwd) {
		return userPwd != null && userPwd.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9]{8,15}$");
	}
	
	private boolean isValidExtension(String extension) {
		return extension != null && extension.matches("\\d{1,4}");
	}
	
}
