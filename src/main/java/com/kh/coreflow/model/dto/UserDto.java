package com.kh.coreflow.model.dto;

import java.util.Date;
import java.util.List;

import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class LoginRequest {
		private String email;
		private String Password;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class AuthResult{
		private String accessToken;
		private String refreshToken;
		private User user;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class User {
		private Long userNo;
		private String userName;
		private String email;
		private String userPwd;
		private List<String> roles;
		private Long depId;
		private Long posId;
		private Date hireDate;
		private String extension;
		private String phone;
		private String address;
		private String addressDetail;
		private Date updateDate;
		private String status;
		private customFile profile;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class FindPwdRequest {
		private String email;
		private String userName;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserUpdate{
		private Long userNo;
		private String userPwd;
		private String phone;
		private String address;
		private String addressDetail;
		private customFile profile;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserCredential {
		private Long userNo;
		private String userPwd;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserAuthority {
		private Long userNo;
		private List<String> roles;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserDeptPoscode {
		private Long userNo;
		private Long depId;
		private Long posId;
		
		@Override
	    public String toString() {
	        return String.valueOf(userNo);
	    }
	}
}