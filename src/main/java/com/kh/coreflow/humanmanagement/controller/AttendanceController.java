package com.kh.coreflow.humanmanagement.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.humanmanagement.model.dto.AttendanceDto.AttendanceInfo;
import com.kh.coreflow.humanmanagement.model.dto.AttendanceDto.PutCheckIn;
import com.kh.coreflow.humanmanagement.model.dto.AttendanceDto.PutCheckOut;
import com.kh.coreflow.humanmanagement.model.dto.AttendanceDto.VacType;
import com.kh.coreflow.humanmanagement.model.dto.AttendanceDto.VacTypeUpdate;
import com.kh.coreflow.humanmanagement.model.service.AttendanceService;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
	private final AttendanceService service;
	
	// 전체 사원 근태 정보 조회
	@CrossOrigin(origins="http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/member")
	public ResponseEntity<List<AttendanceInfo>> allAttendance(
			@RequestParam(value="attDate", required=true) String attDateStr,
			@RequestParam(value="userNo", required=false) Long userNo
			) {
		LocalDate attDate = LocalDate.parse(attDateStr,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		Map<String,Object> params = new HashMap<>();
		params.put("attDate", attDate);
		params.put("userNo", userNo);
		
		List<AttendanceInfo> memAttendance = service.memAttendance(params);
				
		if(memAttendance != null && !memAttendance.isEmpty()) {
			
			return ResponseEntity.ok(memAttendance); 
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 로그인 사용자 근태 정보 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/personal")
	public ResponseEntity<List<AttendanceInfo>> personalAttendance(
			Authentication auth,
			@RequestParam int year,
			@RequestParam int month
			){
		long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
				
		Map<String,Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("year", year);
		params.put("month", month);
		
		List<AttendanceInfo> perAttendance = service.perAttendance(params);
		
		if(perAttendance != null && !perAttendance.isEmpty()) {
			return ResponseEntity.ok(perAttendance);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 출근버튼 클릭시 
	@CrossOrigin(origins="http://localhost:5173")
	@PostMapping("/checkIn")
	public ResponseEntity<Void> checkIn(
			Authentication auth,
			@RequestBody PutCheckIn checkIn
			){
		long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();

		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("checkIn", checkIn);
		
		int result = service.checkIn(params);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 퇴근버튼 클릭시
	@CrossOrigin(origins="http://localhost:5173")
	@PatchMapping("/checkOut")
	public ResponseEntity<Void> checkOut(
			@RequestBody PutCheckOut checkOut
			){
		int result = service.checkOut(checkOut);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 비고 종류 조회
	@CrossOrigin(origins="http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/vacType")
	public ResponseEntity<List<VacType>> vacationType(
			VacType vacType
			){
		List<VacType> vacTypeList = service.vacTypeList(vacType);
		
		if(vacTypeList != null && !vacTypeList.isEmpty()) {
			return ResponseEntity.ok(vacTypeList);
		}else {
			return ResponseEntity.noContent().build();
		}	
	}
	
	// 비고 수정
	@CrossOrigin(origins="http://localhost:5173")
	@PatchMapping("/vacType")
	public ResponseEntity<Void> vacationTypeUpdate(
			@RequestBody VacTypeUpdate vacTypeUpdate
			){		
		int result = service.vacUpdate(vacTypeUpdate);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
}