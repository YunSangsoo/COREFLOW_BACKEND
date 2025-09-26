package com.kh.coreflow.humanmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.humanmanagement.model.dto.VacationDto.AvailableVacations;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.LoginUser;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.MemberChoice;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.MemberVacation;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.PutVacStatus;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.PutVacation;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.VacType;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.VacationInfo;
import com.kh.coreflow.humanmanagement.model.service.VacationService;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class VacationController {
	private final VacationService service;

	// 연차 정보 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@GetMapping("/vacation/info")
	public ResponseEntity<List<VacationInfo>> vacInfo() {
		List<VacationInfo> vacInfoList = service.vacInfo();

		if (vacInfoList != null && !vacInfoList.isEmpty()) {
			return ResponseEntity.ok(vacInfoList);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 모든 사원 휴가 내역 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/vacation/member")
	public ResponseEntity<List<MemberVacation>> allVacation(@RequestParam int year, @RequestParam int month) {
		String formattedMonth = String.format("%02d", month);

		Map<String, Object> params = new HashMap<>();
		params.put("year", year);
		params.put("month", formattedMonth);

		List<MemberVacation> allVacation = service.allVacation(params);

		if (allVacation != null && !allVacation.isEmpty()) {
			return ResponseEntity.ok(allVacation);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 검색 사원 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/vacation/member/search")
	public ResponseEntity<List<MemberChoice>> memberChoice(
			@RequestParam(value = "userName", required = false) String userName) {
		List<MemberChoice> memList = service.memChoice(userName);

		if (memList != null && !memList.isEmpty()) {
			return ResponseEntity.ok(memList);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 검색 사원 휴가 내역 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/vacation/member/{userNo}")
	public ResponseEntity<List<MemberVacation>> MemberVacation(@PathVariable int userNo, @RequestParam int year,
			@RequestParam int month) {
		String formattedMonth = String.format("%02d", month);
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("year", year);
		params.put("month", formattedMonth);

		List<MemberVacation> memVacation = service.memVacation(params);

		if (memVacation != null && !memVacation.isEmpty()) {
			return ResponseEntity.ok(memVacation);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 휴가 상태 변경
	@CrossOrigin(origins = "http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@PatchMapping("/vacation/member/{vacId}")
	public ResponseEntity<Void> vacStatusUpdate(@PathVariable int vacId, @RequestBody PutVacStatus putVacStatus) {
		Map<String, Object> params = new HashMap<>();
		params.put("vacId", vacId);
		params.put("putVacStatus", putVacStatus);

		int result = service.vacStatusUpdate(params);

		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 로그인 회원 정보 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@GetMapping("/user/profile")
	public ResponseEntity<LoginUser> loginUserProfile(Authentication auth) {
		long userNo = ((UserDeptPoscode) auth.getPrincipal()).getUserNo();
		LoginUser loginUser = service.loginUserProfile(userNo);

		if (loginUser != null) {
			return ResponseEntity.ok(loginUser);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 로그인 회원 휴가 내역 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@GetMapping("/vacation/personal")
	public ResponseEntity<List<MemberVacation>> personalVacation(Authentication auth, @RequestParam int year) {
		long userNo = ((UserDeptPoscode) auth.getPrincipal()).getUserNo();

		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("year", year);

		List<MemberVacation> perVacation = service.perVacation(params);

		if (perVacation != null && !perVacation.isEmpty()) {
			return ResponseEntity.ok(perVacation);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 로그인 회원 사용 가능 휴가 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/vacation/personal-available")
	public ResponseEntity<AvailableVacations> availableVacations(
			Authentication auth,
			@RequestParam int year
			){
		long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("year", year);
		
		AvailableVacations availableVac = service.availableVacations(params);
		
		if (availableVac != null) {
			return ResponseEntity.ok(availableVac);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 휴가 종류 조회
	@CrossOrigin(origins = "http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/vacation/type")
	public ResponseEntity<List<VacType>> vacType() {
		List<VacType> vacList = service.vacType();

		if (vacList != null && !vacList.isEmpty()) {
			return ResponseEntity.ok(vacList);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	// 로그인 회원 휴가 신청
	@CrossOrigin(origins = "http://localhost:5173")
	@PutMapping("/vacation/personal")
	public ResponseEntity<Void> putPerVac(Authentication auth, @RequestBody PutVacation putVacation) {
		long userNo = ((UserDeptPoscode) auth.getPrincipal()).getUserNo();

		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("putVacation", putVacation);

		int result = service.putPerVac(params);

		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
}