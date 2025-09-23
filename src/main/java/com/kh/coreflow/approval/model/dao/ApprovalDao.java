package com.kh.coreflow.approval.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.approval.model.dto.ApprovalDto;
import com.kh.coreflow.approval.model.dto.ApprovalFileDto;
import com.kh.coreflow.approval.model.dto.ApprovalLineDto;

@Repository
public class ApprovalDao {

    @Autowired
    private SqlSessionTemplate session;

    // 문서
    public int insertApproval(ApprovalDto approval) {
        return session.insert("approvalMapper.insertApproval", approval);
    }
    public ApprovalDto findById(int approvalId) {
        return session.selectOne("approvalMapper.findById", approvalId);
    }
    public int updateApprovalStatus(ApprovalDto approval) {
        return session.update("approvalMapper.updateApprovalStatus", approval);
    }
    public List<ApprovalDto> selectAllApprovals() {
        return session.selectList("approvalMapper.selectAllApprovals");
    }

    // 결재라인
    public int insertApprovalLine(ApprovalLineDto line) {
        return session.insert("approvalMapper.insertApprovalLine", line);
    }
    public List<ApprovalLineDto> findLinesByApprovalId(int approvalId) {
        return session.selectList("approvalMapper.findLinesByApprovalId", approvalId);
    }
    public List<ApprovalLineDto> findWaitingLinesByApprover(int approvalId, int approverUserId) {
        HashMap<String,Object> param = new HashMap<>();
        param.put("approvalId", approvalId);
        param.put("userId", approverUserId);
        param.put("status", "WAITING");
        return session.selectList("approvalMapper.findWaitingLinesByApprover", param);
    }
    public ApprovalLineDto findLineByApprovalIdAndOrder(int approvalId, int lineOrder) {
        HashMap<String,Object> param = new HashMap<>();
        param.put("approvalId", approvalId);
        param.put("lineOrder", lineOrder);
        return session.selectOne("approvalMapper.findLineByApprovalIdAndOrder", param);
    }
    
    // 추가된 메서드: 결재선 상태 업데이트
    public int updateApprovalLineStatus(int lineId, String status) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("lineId", lineId);
        param.put("status", status);
        return session.update("approvalMapper.updateApprovalLineStatus", param);
    }

    // 파일
    public int insertApprovalFile(ApprovalFileDto file) {
        return session.insert("approvalMapper.insertApprovalFile", file);
    }
    public List<ApprovalFileDto> findFilesByApprovalId(int approvalId) {
        return session.selectList("approvalMapper.findFilesByApprovalId", approvalId);
    }
    //======================================================
	public List<ApprovalDto> findPendingApprovals(int userId) {
		return session.selectList("approvalMapper.findPendingApprovals", userId);
	}
	// 내문서 조회
	public List<ApprovalDto> selectApprovalsByUserNo(int userNo, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("keyword", keyword);
		return session.selectList("approvalMapper.selectApprovalsByUserNo", params);
	}
	// 받은 문서함
	public List<ApprovalDto> selectReceivedApprovalsByApproverNo(int userNo, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("keyword", keyword);		
		return session.selectList("approvalMapper.selectReceivedApprovalsByApproverNo", params);
	}
	// 결재완료 문서함
	public List<ApprovalDto> selectProcessedApprovalsByApproverNo(int userNo, String keyword){
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("keyword", keyword);
		return session.selectList("approvalMapper.selectProcessedApprovalsByApproverNo", params);
	}
	// 유저 이름가져오기
	public String findUserNameByUserNo(int userNo) {
        return session.selectOne("approvalMapper.findUserNameByUserNo", userNo);
    }
	// 참조문서함
	public List<ApprovalDto> selectCcApprovalsByApproverNo(int userNo, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("keyword", keyword);
		return session.selectList("approvalMapper.selectCcApprovalsByApproverNo", params);
	}
	// 파일다운로드
	public ApprovalFileDto findFileById(int fileId) {
		return session.selectOne("approvalMapper.findFileById", fileId);
	}
	// 사이드바 알림
	public int selectReceivedApprovalsCount(int userNo) {
		return session.selectOne("approvalMapper.selectReceivedApprovalsCount", userNo);
	}
	// 순차결재
	public ApprovalLineDto findWaitingLineByApprover(int approvalId, int approverUserId) {
		Map<String, Object> params = new HashMap<>();
		
		params.put("approvalId", approvalId);
		params.put("approverUserId", approverUserId);
		return session.selectOne("approvalMapper.findWaitingLineByApprover", params);
	}
	
	
}