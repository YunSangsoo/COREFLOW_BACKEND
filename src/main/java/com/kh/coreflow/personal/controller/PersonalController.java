package com.kh.coreflow.personal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kh.coreflow.model.dao.AuthDao;
import com.kh.coreflow.model.dto.UserDto.User;
import com.kh.coreflow.model.dto.UserDto.UserAuthority;
import com.kh.coreflow.personal.model.service.UserService;
import com.kh.coreflow.security.CustomUserDetails;
import com.kh.coreflow.security.model.service.AuthService;

@Controller
@RequestMapping("/api/mypage")
public class PersonalController {

	private final AuthService authService;
	private final UserService userService;
	private final AuthDao authDao;
	
	public PersonalController(AuthService authService, UserService userService, AuthDao authDao) {
		this.authService = authService;
		this.userService = userService;
		this.authDao = authDao;
	}
	
	@GetMapping
	public String myPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userNo = userDetails.getUserNo();
		Optional<User> user = authService.findUserByUserNo(userNo);
		model.addAttribute("user" ,user);
		
		UserAuthority userAuth = authDao.findUserAuthorityByUserNo(userNo);
		if (userAuth == null) {
		    throw new IllegalArgumentException("권한 정보 없음");
		}
		Long depId = userDetails.getDepId();
		
		List<Object> mySchedule = userService.getMySchedule(userNo);
		model.addAttribute("mySchedule" ,mySchedule);
		
		return "/mypage";
	}
	
	
}














