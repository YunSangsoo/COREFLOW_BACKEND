package com.kh.coreflow.companypolicy.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.companypolicy.model.dto.CompanyPolicyDto.*;
import com.kh.coreflow.companypolicy.model.service.CompanyPolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins="http://localhost:5173", exposedHeaders="Location")
@RequestMapping("/cpolicies")
@Tag(name="Company Policy API", description="회사 규정 API")
public class CompanyPolicyController {
	private final CompanyPolicyService service;
	
	@GetMapping
	@Operation(summary="회사 규정 목록 조회.", description="회사 규정 목록 조회.")
	@ApiResponses({
		@ApiResponse(responseCode="200", description="조회 성공."),
		@ApiResponse(responseCode="404", description="조회 실패.")
	})
	public ResponseEntity<List<CompanyPolicy>> cpolicies() {
		List<CompanyPolicy> list = service.getPolicies();
		
		if (list.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(list);
		}
	}
	
	@PostMapping
	@Operation(summary="규정 추가.", description="입력한 회사 규정을 추가함.")
	@ApiResponses({
		@ApiResponse(responseCode="201", description="추가 성공."),
		@ApiResponse(responseCode="400", description="추가 실패.")
	})
	public ResponseEntity<Map<String, Object>> addPolicy(
			@RequestBody CompanyPolicy policy
			) {
		policy.setCreatorUserNo(1L);
		log.info("policy: {}", policy);
		
//		return ResponseEntity.ok(null);
		int result = service.addPolicy(policy);
		log.info("policy after: {}", policy);
		
		Map<String, Object> map = new HashMap<>();
		map.put("policyId", policy.getPolicyId());
		
		if (result > 0) {
			URI location = URI.create("/cpolicies/" + policy.getPolicyId());
			return ResponseEntity.created(location).body(map);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@PatchMapping("/{policyId}")
	@Operation(summary="규정 수정.", description="해당 규정을 수정함.")
	@ApiResponses({
		@ApiResponse(responseCode="204", description="수정 성공."),
		@ApiResponse(responseCode="400", description="수정 실패.")
	})
	public ResponseEntity<Void> updatePolicy(
			@RequestBody CompanyPolicyModHistory history,
			@PathVariable Long policyId
			) {
		history.setUserNo(1L);
		history.setPolicyId(policyId);
		
		int result = service.updatePolicy(history);
		
		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
}









