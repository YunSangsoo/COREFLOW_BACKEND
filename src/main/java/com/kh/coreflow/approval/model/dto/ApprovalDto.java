package com.kh.coreflow.approval.model.dto;

import java.util.Date;
import java.util.List;

import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.Data;

@Data
public class ApprovalDto {	
	
	private int approvalId;
    private int userNo; // 작성자 ID
    private String approvalTitle;
    private String approvalDetail;
    private String approvalType;
    private int approvalStatus; // 0:임시저장, 1:진행, 2:승인, 3:반려
    private Date startDate;
    private Date endDate;
    private Date saveDate;		// 임시저장날짜
    private String status;	// 문서 상태
    private String comment; // 승인,반려 사유 작성가능
    private String userName;
    private String processedStatus;
    
    private List<Integer> approverUserNo;
    private List<Integer> ccUserNo;
	
	
	private List<ApprovalLineDto> lines;
	
	//private List<ApprovalFileDto> files;
	private List<customFile> files;


	

	
	
}













