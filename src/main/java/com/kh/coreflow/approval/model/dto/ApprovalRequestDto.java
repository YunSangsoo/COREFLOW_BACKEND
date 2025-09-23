package com.kh.coreflow.approval.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ApprovalRequestDto {
	private String approvalTitle;
	private String approvalDetail;
	private String approvlaType;
	private List<Integer> approverUserNos; // 결재자 id목록
}
