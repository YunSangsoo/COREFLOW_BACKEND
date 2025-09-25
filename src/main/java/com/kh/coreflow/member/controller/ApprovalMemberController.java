package com.kh.coreflow.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.member.model.dto.ApprovalMemberDto;
import com.kh.coreflow.member.model.dto.DepartmentDto;
import com.kh.coreflow.member.service.ApprovalMemberService;

@RestController
@RequestMapping("/apporval-membrs")
public class ApprovalMemberController {

	private final ApprovalMemberService memberService;
	
	@Autowired
	public ApprovalMemberController(ApprovalMemberService memberService) {
		this.memberService = memberService;
	}
	
	@GetMapping("/departments")
	public List<DepartmentDto> getDepartments(){
		return memberService.getAllDepartments();
	}
	
	@GetMapping("/members")
	public List<ApprovalMemberDto> searchMembers(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) Integer depId,
			@RequestParam(defaultValue = "50") Integer limit){
		return memberService.searchMember(query, depId, limit);
	}
	
}









