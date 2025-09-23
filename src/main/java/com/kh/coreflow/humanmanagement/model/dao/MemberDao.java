package com.kh.coreflow.humanmanagement.model.dao;

import java.util.List;
import java.util.Map;

import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Department;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.DepartmentLite;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberLite;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPatch;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberResponse;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Position;
import com.kh.coreflow.model.dto.UserDto.User;

public interface MemberDao {

	List<Department> deptList();

	List<Department> deptDetailList(int parentId);
	
	List<Position> posiList();

	List<MemberResponse> memberList(Map<String, String> searchParams);

	MemberResponse memberDetail(Long userNo);

	int memberInsert(User user);
	
	int memberUpdate(MemberPatch member);

	int memberDelete(Long userNo);
	
	Long findDepId(String depName);

	Long findPodId(String posName);

	List<MemberLite> searchMembers(String query, Integer limit, Long depId);

	List<DepartmentLite> findAll();
}