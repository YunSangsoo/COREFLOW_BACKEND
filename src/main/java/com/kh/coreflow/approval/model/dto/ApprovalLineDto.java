package com.kh.coreflow.approval.model.dto;

import java.util.Date;

import lombok.Data;

//결재선
@Data
public class ApprovalLineDto {
	private int lineId;
    private long approvalId;
    private int lineOrder; // 결재 순서
    private int approverUserNo;
    private String lineStatus; // WAITING(대기), PENDING(예정), APPROVED(승인), REJECTED(반려)
    private String userName;
}
