package com.kh.coreflow.humanmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberResponse;
import com.kh.coreflow.humanmanagement.model.dto.OrganizationDto.ChildDep;
import com.kh.coreflow.humanmanagement.model.dto.OrganizationDto.ParentDep;
import com.kh.coreflow.humanmanagement.model.service.OrganizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/organization")
public class OrganizationController {
	private final OrganizationService service;
	
	// 1. 부서 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/departments")
	public ResponseEntity<List<ParentDep>> parentDepartment(){
		List<ParentDep> parentDeptList = service.parentDeptList();

		if(parentDeptList != null && !parentDeptList.isEmpty()) {
			return ResponseEntity.ok(parentDeptList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 2. 자식 부서 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/departments/{parentId}")
	public ResponseEntity<List<ChildDep>> childDepartment(
			@PathVariable int parentId
			){
		List<ChildDep> childDeptList = service.childDeptList(parentId);

		if(childDeptList != null && !childDeptList.isEmpty()) {
			return ResponseEntity.ok(childDeptList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 3. 부서별 사원 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/members/{depId}")
	public ResponseEntity<List<MemberResponse>> member(
			@PathVariable int depId
			){
		List<MemberResponse> memberList = service.memberList(depId);
		
		if(memberList != null && !memberList.isEmpty()) {
			return ResponseEntity.ok(memberList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
}	