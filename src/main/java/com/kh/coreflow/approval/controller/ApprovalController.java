package com.kh.coreflow.approval.controller;

import org.springframework.http.HttpHeaders; 
import org.springframework.core.io.Resource; 

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.approval.model.dto.ApprovalDto;
import com.kh.coreflow.approval.model.dto.ApprovalFileDto;
import com.kh.coreflow.approval.model.service.ApprovalService;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
class ApprovalRequest {
    private String action; 
}

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/approvals")
@Tag(name = "Approval API", description = "")
public class ApprovalController {

    private final ApprovalService service;
    
    private int getUserNoFromPrincipal(Principal principal) {
        Authentication authentication = (Authentication) principal;
        UserDeptPoscode userDetails = (UserDeptPoscode) authentication.getPrincipal();
        return (userDetails.getUserNo()).intValue();
    }

    // --- API ---

    @Operation(summary = "결재문서 작성")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> submitApproval(
            @RequestPart("approvalData") ApprovalDto approval,
            @RequestPart(value = "files", required = false) MultipartFile file,
            Principal principal) {
        
    	if (approval.getApprovalStatus() == 1 && (approval.getApprovalTitle() == null || approval.getApprovalTitle().trim().isEmpty())) {
            return ResponseEntity.badRequest().body("제목은 필수입력 항목입니다");
        }
        int userNo = getUserNoFromPrincipal(principal);
        int approvalId = service.submitApproval(approval, file, userNo);
        return ResponseEntity.ok(approvalId);
    }

    @Operation(summary = "결재 처리 (승인/부결)")
    @PostMapping("/{approvalId}/process")
    public ResponseEntity<String> processApproval(
            @PathVariable("approvalId") int approvalId,
            @RequestBody ApprovalRequest request,
            Principal principal) {

        try {
           
            int approverUserId = getUserNoFromPrincipal(principal);

            service.processApproval(approvalId, approverUserId, request.getAction());

            return ResponseEntity.ok("결재가 성공적으로 처리되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("결재 처리 중 오류 발생", e); 
            return ResponseEntity.status(500).body("결재 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "현재 로그인 유저의 대기 중 문서 목록 조회")
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalDto>> getPendingApprovals(Principal principal) {
        int userNo = getUserNoFromPrincipal(principal);
        List<ApprovalDto> pending = service.getPendingApprovals(userNo);
        return ResponseEntity.ok(pending);
    }

    @Operation(summary = "내문서함 문서 조회")
    @GetMapping("/my-documents")
    public ResponseEntity<List<ApprovalDto>> getDocuments(
            Principal principal,
            @RequestParam(value = "keyword", required = false) String keyword) { 
        int userNo = getUserNoFromPrincipal(principal);
        List<ApprovalDto> documents = service.getDocumentsByUser(userNo, keyword); 
        return ResponseEntity.ok(documents);
    }
    //결재
    @Operation(summary = "결재문서 상세 조회")
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Map<String, Object>> getApprovalDetails(
    		@PathVariable("id") int approvalId,
    		Principal principal) {
    	
    	ApprovalDto approval = service.getApprovalDetails(approvalId);
    	
        if (approval == null) {
            return ResponseEntity.notFound().build();
        }
        
        int currentUserNo = getUserNoFromPrincipal(principal);
        
        boolean isCurrentUserApprover = approval.getLines().stream()
        		.anyMatch(line -> "WAITING".equals(line.getLineStatus()) && line.getApproverUserNo() == currentUserNo);
        
        Map<String, Object> response = new HashMap<>();
        
        response.put("approval", approval);
        response.put("currentUserIsApprover", isCurrentUserApprover);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "결재문서 상태 수정 (관리자용)")
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
    		@PathVariable("id") int approvalId,
    		@RequestBody Map<String, Integer> payload){
  
    	try {
            Integer newStatus = payload.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body("변경할 상태 정보가 없습니다.");
            }
            service.updateApprovalStatus(approvalId, newStatus);
            return ResponseEntity.ok("문서 상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            log.error("상태 변경 처리 중 오류 발생", e);
            return ResponseEntity.status(500).body("서버 오류로 인해 상태 변경에 실패했습니다.");
        	}
    	}
    

    @Operation(summary = "받은문서함")
    @GetMapping("/received-documents")
    public ResponseEntity<List<ApprovalDto>> getReceivedDocuments(
            Principal principal,
            @RequestParam(value = "keyword", required = false) String keyword) { 
        int userNo = getUserNoFromPrincipal(principal);
        List<ApprovalDto> documents = service.getReceivedDocumentsByUser(userNo, keyword); 
        return ResponseEntity.ok(documents);
    }
    
    @Operation(summary = "결재완료함")
    @GetMapping("/processed-documents")
    public ResponseEntity<List<ApprovalDto>> getProcessedDocuments(
            Principal principal,
            @RequestParam(value = "keyword", required = false) String keyword){ 
    	int userNo = getUserNoFromPrincipal(principal);
    	List<ApprovalDto> documents = service.getProcessedDocumentsByUser(userNo, keyword); 
    	return ResponseEntity.ok(documents);
    }
    
    @Operation(summary = "참조문서함")
    @GetMapping("/cc-documents")
    public ResponseEntity<List<ApprovalDto>> getCcDocuments(
            Principal principal,
            @RequestParam(value = "keyword", required = false) String keyword){
    	int userNo = getUserNoFromPrincipal(principal);
    	List<ApprovalDto> documents = service.getCcDocumentsByUser(userNo, keyword);
    	return ResponseEntity.ok(documents);
    }
    
    //문서에 첨부된 파일 다운로드 컨트롤러
    @Operation(summary = "첨부파일 다운로드")
    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable int fileId){
    	
    	ApprovalFileDto fileDto = service.findFileById(fileId);
    	
    	if (fileDto == null) {
    		return ResponseEntity.notFound().build();
    	}
    	
    	try {
            Path filePath = Paths.get(fileDto.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String originalFileName = fileDto.getOriginalFileName();
                String encodedFileName = new String(originalFileName.getBytes("UTF-8"), "ISO-8859-1");

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("파일을 읽을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            throw new RuntimeException("파일 다운로드 중 오류 발생: " + e.getMessage());
        }
    }
    
    @Operation(summary = "받은문서함 알림")
    @GetMapping("/received-documents/count")
    public ResponseEntity<Map<String,Integer>> getReceivedDocumentsCount(Principal principal){
    	int userNo = getUserNoFromPrincipal(principal);
    	int count = service.getReceivedApprovalsCount(userNo);
    	Map<String, Integer> response = new HashMap<>();
    	response.put("count", count);
    	return ResponseEntity.ok(response);
    }
    
}











