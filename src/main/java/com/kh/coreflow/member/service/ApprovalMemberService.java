package com.kh.coreflow.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.coreflow.member.dao.ApprovalMemberDao;
import com.kh.coreflow.member.model.dto.ApprovalMemberDto;
import com.kh.coreflow.member.model.dto.DepartmentDto;

@Service
public class ApprovalMemberService {
	
	private final ApprovalMemberDao memberDao;
	
	@Autowired
	public ApprovalMemberService(ApprovalMemberDao memberDao) {
		this.memberDao = memberDao;
	}
	
	public List<DepartmentDto> getAllDepartments(){
		return memberDao.findAllDepartments();
	}
	
	public List<ApprovalMemberDto> searchMember(String query, Integer depId, Integer limit){
		return memberDao.findMembers(query, depId, limit);
	}
}
