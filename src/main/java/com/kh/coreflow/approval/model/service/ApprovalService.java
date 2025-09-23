package com.kh.coreflow.approval.model.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.approval.model.dao.ApprovalDao;
import com.kh.coreflow.approval.model.dto.ApprovalDto;
import com.kh.coreflow.approval.model.dto.ApprovalFileDto;
import com.kh.coreflow.approval.model.dto.ApprovalLineDto;
import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalDao dao;
//    @Value("${file.upload-dir}")
//    private String uploadPath;
    
//    public ApprovalService(ApprovalDao dao) {
//        this.dao = dao;
//    }
    
    private final FileService fileService;

    @Transactional
    public int submitApproval(ApprovalDto approval,MultipartFile file, int userNo) {
 
    	String userName = dao.findUserNameByUserNo(userNo);
    	approval.setUserName(userName);
    	approval.setUserNo(userNo);
        approval.setSaveDate(new Date());
        dao.insertApproval(approval);

        // 결재선 정보 순서결재
        if (approval.getApproverUserNo() != null) {
        	int order = 1;
            for (Integer approverNo : approval.getApproverUserNo()) {
            	ApprovalLineDto line = new ApprovalLineDto();
                line.setApprovalId(approval.getApprovalId());
                line.setApproverUserNo(approverNo);
                
                line.setLineOrder(order);
                
                if (order == 1) {
                	line.setLineStatus("WAITING");                	
                } else {
                	line.setLineStatus("PENDING");                	                	
                }
                dao.insertApprovalLine(line);
                order++;
            }
        }
        // 참조자 저장
        if (approval.getCcUserNo() != null) {
        	for (Integer ccNo : approval.getCcUserNo()) {
        		ApprovalLineDto line = new ApprovalLineDto();
        		line.setApprovalId(approval.getApprovalId());
        		line.setApproverUserNo(ccNo);
        		line.setLineOrder(99);
        		line.setLineStatus("CC");
        		dao.insertApprovalLine(line);
        	}
        }

        // 파일정보
        if (file != null && !file.isEmpty()) {
        	customFile getFile = fileService.setOrChangeOneImage(file, Long.valueOf(approval.getApprovalId()),"A");
        }

        return approval.getApprovalId();
    }
    
    //순차결재
    @Transactional
    public void processApproval(int approvalId, int approverUserId, String action) {
        ApprovalLineDto currentLine = dao.findWaitingLineByApprover(approvalId, approverUserId);

        if (currentLine == null) {
            throw new IllegalStateException("결재할 순서가 아니거나 이미 처리된 문서입니다.");
        }

        if ("APPROVE".equalsIgnoreCase(action)) {
            dao.updateApprovalLineStatus(currentLine.getLineId(), "APPROVED");

            ApprovalLineDto nextLine = dao.findLineByApprovalIdAndOrder(approvalId, currentLine.getLineOrder() + 1);

            if (nextLine != null) {
                dao.updateApprovalLineStatus(nextLine.getLineId(), "WAITING");
            } else {
                ApprovalDto approval = new ApprovalDto();
                approval.setApprovalId(approvalId);
                approval.setApprovalStatus(2); // 2: 승인
                dao.updateApprovalStatus(approval);
            }

        } else if ("REJECT".equalsIgnoreCase(action)) {
            dao.updateApprovalLineStatus(currentLine.getLineId(), "REJECTED");
            
            ApprovalDto approval = new ApprovalDto();
            approval.setApprovalId(approvalId);
            approval.setApprovalStatus(3); // 3: 반려
            dao.updateApprovalStatus(approval);

        } else {
            throw new IllegalArgumentException("action은 'APPROVE' 또는 'REJECT'만 가능합니다.");
        }
    }

    public List<ApprovalDto> getAllDocuments() {
        return dao.selectAllApprovals();
    }
    
    public List<ApprovalDto> getPendingApprovals(int userId) {
        return dao.findPendingApprovals(userId);
    }
    
    public List<ApprovalLineDto> getApprovalLines(int approvalId){
    	return dao.findLinesByApprovalId(approvalId);
    }
    
    public List<ApprovalFileDto> getApprovalFiles(int approvalId){
    	return dao.findFilesByApprovalId(approvalId);
    }
    // 내문서함
	public List<ApprovalDto> getDocumentsByUser(int userNo, String keyword) {
		return dao.selectApprovalsByUserNo(userNo, keyword);
	}

	public ApprovalDto getApprovalDetails(int approvalId) {
		
		ApprovalDto approval = dao.findById(approvalId);
		
		if(approval != null) {
			approval.setLines(dao.findLinesByApprovalId(approvalId));
			approval.setFiles(fileService.getFiles("A", Long.valueOf(approvalId)));
			//approval.setFiles(dao.findFilesByApprovalId(approvalId));
		}
		return approval;
		
	}
	// 받은문서함
	public List<ApprovalDto> getReceivedDocumentsByUser(int userNo, String keyword) {
		return dao.selectReceivedApprovalsByApproverNo(userNo, keyword);
	}

	public void updateApprovalStatus(int approvalId, Integer newStatus) {
		
		ApprovalDto approvalToUpdate = new ApprovalDto();
		
		approvalToUpdate.setApprovalId(approvalId);
		approvalToUpdate.setApprovalStatus(newStatus);
		
		int result = dao.updateApprovalStatus(approvalToUpdate);
		
		if (result == 0) {
			throw new RuntimeException("문서 상태변경 실패 (ID: \" + approvalId + \")");
		}
	}
	// 결재 완료함
	public List<ApprovalDto> getProcessedDocumentsByUser(int userNo, String keyword){
		return dao.selectProcessedApprovalsByApproverNo(userNo, keyword);
	}
	
	

	private void insertLinesAndFiles(ApprovalDto approval, MultipartFile file) {
        // 결재선 정보
        if (approval.getApproverUserNo() != null) {
            for (Integer approverNo : approval.getApproverUserNo()) {
                ApprovalLineDto line = new ApprovalLineDto();
                line.setApprovalId(approval.getApprovalId());
                line.setApproverUserNo(approverNo);
                line.setLineOrder(1);
                line.setLineStatus("WAITING");
                dao.insertApprovalLine(line);
            }
        }
        // 참조자 저장
        if (approval.getCcUserNo() != null) {
            for (Integer ccNo : approval.getCcUserNo()) {
                ApprovalLineDto line = new ApprovalLineDto();
                line.setApprovalId(approval.getApprovalId());
                line.setApproverUserNo(ccNo);
                line.setLineOrder(2);
                line.setLineStatus("PENDING");
                dao.insertApprovalLine(line);
            }
        }
        if (file != null && !file.isEmpty()) {
        }
    }
	// 참조문서함
	public List<ApprovalDto> getCcDocumentsByUser(int userNo, String keyword) {
		return dao.selectCcApprovalsByApproverNo(userNo, keyword);
	}
	// 파일 다운로드
	public ApprovalFileDto findFileById(int fileId) {
		return dao.findFileById(fileId);
	}
	// 사이드바 알림
	public int getReceivedApprovalsCount(int userNo) {
		return dao.selectReceivedApprovalsCount(userNo);
	}
}

	
    
    
    
    
    
    
    
