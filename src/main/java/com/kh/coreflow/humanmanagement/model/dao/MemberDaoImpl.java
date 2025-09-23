package com.kh.coreflow.humanmanagement.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Department;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.DepartmentLite;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberLite;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPatch;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberResponse;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Position;
import com.kh.coreflow.model.dto.UserDto.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberDaoImpl implements MemberDao {
	private final SqlSessionTemplate session;

	@Override
	public List<Department> deptList() {
		return session.selectList("member.deptList");
	}

	@Override
	public List<Department> deptDetailList(int parentId) {
		return session.selectList("member.deptDetailList", parentId);
	}

	@Override
	public List<Position> posiList() {
		return session.selectList("member.posiList");
	}

	@Override
	public List<MemberResponse> memberList(Map<String, String> searchParams) {
		return session.selectList("member.memberList", searchParams);
	}

	@Override
	public MemberResponse memberDetail(Long userNo) {
		return session.selectOne("member.memberDetail", userNo);
	}

	@Override
	public int memberInsert(User user) {
		return session.insert("member.memberInsert", user);
	}

	@Override
	public int memberUpdate(MemberPatch member) {
		return session.update("member.memberUpdate", member);
	}

	@Override
	public int memberDelete(Long userNo) {
		return session.delete("member.memberDelete", userNo);
	}

	@Override
	public List<MemberLite> searchMembers(String query, Integer limit, Long depId) {
		Map<String, Object> p = new HashMap<>();
		p.put("query", query);
		p.put("limit", limit);
		p.put("depId", depId);

		return session.selectList("member.searchMembers", p);
	}

	@Override
	public List<DepartmentLite> findAll() {
		return session.selectList("member.findAll");
	}

	@Override
	public Long findDepId(String depName) {
		return session.selectOne("member.findDepId", depName);
	}

	@Override
	public Long findPodId(String posName) {
		return session.selectOne("member.findPodId", posName);
	}
}