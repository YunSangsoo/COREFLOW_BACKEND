package com.kh.coreflow.member.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.member.model.dto.ApprovalMemberDto;
import com.kh.coreflow.member.model.dto.DepartmentDto;

@Repository
public class ApprovalMemberDao {
	
	private final SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
    public ApprovalMemberDao(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public List<DepartmentDto> findAllDepartments() {
        return sqlSessionTemplate.selectList("MemberMapper.findAllDepartments");
    }

    public List<ApprovalMemberDto> findMembers(String query, Integer depId, Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("depId", depId);
        params.put("limit", limit);

        return sqlSessionTemplate.selectList("MemberMapper.findMembers", params);
    }
}









