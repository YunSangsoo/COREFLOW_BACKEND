package com.kh.coreflow.humanmanagement.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Department;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPatch;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPost;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberResponse;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Position;
import com.kh.coreflow.humanmanagement.model.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MemberController {
	private final MemberService service;
	
	// 부모 부서 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/departments")
	public ResponseEntity<List<Department>> deptList(){
		List<Department> deptList = service.deptList();
		
		if(deptList != null && !deptList.isEmpty()) {
			return ResponseEntity.ok(deptList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 자식 부서 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/departments/{parentId}")
	public ResponseEntity<List<Department>> deptDetailList(
			@PathVariable int parentId
			){
		List<Department> deptDetailList = service.deptDetailList(parentId);
		
		if(deptDetailList != null && !deptDetailList.isEmpty()) {
			return ResponseEntity.ok(deptDetailList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 직위 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/positions")
	public ResponseEntity<List<Position>> posiList(){
		List<Position> posiList = service.posiList();
		
		if(posiList != null) {
			return ResponseEntity.ok(posiList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 사원 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/members")
	public ResponseEntity<List<MemberResponse>> memberList(
			@RequestParam Map<String,String> searchParams
			){		
		List<MemberResponse> memberList = service.memberList(searchParams);
		
		if(!memberList.isEmpty()) {
			return ResponseEntity.ok(memberList);
		}else {
			return ResponseEntity.ok(memberList);
		}
	}
	
	// 사원 상세 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/members/{userNo}")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	public ResponseEntity<MemberResponse> memberDetail(
			@PathVariable int userNo
			){
		MemberResponse member = service.memberDetail(userNo);
		
		if(member != null) {
			return ResponseEntity.ok(member);
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 사원 등록
	@CrossOrigin(origins="http://localhost:5173")
	@PostMapping("/members")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	public ResponseEntity<Void> memberInsert(
			@RequestBody MemberPost member
			){
		int result = service.memberInsert(member);
		
		if(result > 0) {
			return ResponseEntity.created(URI.create("/members")).build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 사원 정보 수정
	@CrossOrigin(origins="http://localhost:5173")
	@PatchMapping("/members/{userNo}")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	public ResponseEntity<Void> memberUpdate(
			@PathVariable int userNo,
			@RequestBody MemberPatch member
			){
		member.setUserNo(userNo);
		log.info("userNo : {}",userNo);
		log.info("member : {}",member);
		int result = service.memberUpdate(member);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 사원 정보 삭제
	@CrossOrigin(origins="http://localhost:5173")
	@DeleteMapping("members/{userNo}")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	public ResponseEntity<Void> memberDelete(
			@PathVariable int userNo
			) {
		int result = service.memberDelete(userNo);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
}