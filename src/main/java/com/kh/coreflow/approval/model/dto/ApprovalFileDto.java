package com.kh.coreflow.approval.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//파일첨부
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalFileDto {
	private int fileId;
    private long approvalId;
    private String OriginalFileName;
    private String savedFileName;
    private String filePath;
    private long fileSize;
	
	
}
