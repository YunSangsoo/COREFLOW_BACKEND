package com.kh.coreflow.ai.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.coreflow.ai.model.dto.AiDto.AiChatHistory;
import com.kh.coreflow.ai.model.dto.AiDto.AiChatSession;
import com.kh.coreflow.ai.model.dto.AiDto.AiUsage;
import com.kh.coreflow.ai.model.service.AiService;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
@Tag(name="CoreFlow AI API", description="CoreFlow AI API")
public class AiController {
	private final AiService service;
	
	@GetMapping("/sessions")
	public ResponseEntity<List<AiChatSession>> getSessions(
			Authentication auth
			) {
		Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		
		List<AiChatSession> list = service.getSessions(userNo);
		
		if (list != null) {
			return ResponseEntity.ok(list);			
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/checkUsedBefore")
	public ResponseEntity<Boolean> checkUsedBefore(
			Authentication auth
			) {
		Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		
		Integer count = service.countAiUsage(userNo);
		
		if (count != null) {
			if (count > 0) {
				return ResponseEntity.ok(true);				
			} else {
				return ResponseEntity.ok(false);
			}
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PostMapping("/insertAiUsage")
	public ResponseEntity<Void> insertAiUsage(
			Authentication auth,
			@RequestBody AiUsage usage
			) {
		Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		
		Map<String, Object> map = new HashMap<>();
		map.put("userNo", userNo);
		map.put("tokensUsed", usage.getTokensUsed());
		
		int result = service.insertAiUsage(map);
		
		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@PatchMapping("/updateAiUsage")
	public ResponseEntity<Void> updateAiUsage(
			Authentication auth,
			@RequestBody AiUsage usage
			) {
		Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		
		Map<String, Object> map = new HashMap<>();
		map.put("userNo", userNo);
		map.put("tokensUsed", usage.getTokensUsed());
		
		int result = service.updateAiUsage(map);
		
		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@PostMapping("/sessions")
	public ResponseEntity<Long> insertSession(
			Authentication auth,
			@RequestBody AiChatSession chatSession
			) {
		Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		
		Map<String, Object> map = new HashMap<>();
		map.put("userNo", userNo);
		map.put("sessionId", 0L);
		map.put("title", chatSession.getTitle());
		
		int result = service.insertSession(map);
		
		if (result > 0) {
			return ResponseEntity.ok(Long.parseLong(map.get("sessionId").toString()));
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@PatchMapping("/sessions/{sessionId}")
	public ResponseEntity<Void> updateSession(
			@PathVariable Long sessionId
			) {
		int result = service.updateSession(sessionId);
		
		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/{sessionId}/histories")
	public ResponseEntity<List<AiChatHistory>> getHistories(
			@PathVariable Long sessionId
			) {
		List<AiChatHistory> list = service.getHistories(sessionId);
		
		if (list != null) {
			return ResponseEntity.ok(list);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PostMapping("/{sessionId}/histories")
	public ResponseEntity<Void> insertHistory(
			Authentication auth,
			@PathVariable Long sessionId,
			@RequestBody AiChatHistory history
			) {
		Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
		Map<String, Object> map = new HashMap<>();
		map.put("userNo", userNo);
		map.put("sessionId", sessionId);
		map.put("role", history.getRole());
		map.put("content", history.getContent());
		
		int result = service.insertHistory(map);
		
		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@DeleteMapping("/{sessionId}")
	public ResponseEntity<Void> deleteChatSession(
			@PathVariable Long sessionId
			) {
		int result = service.deleteChatSession(sessionId);
		
		if (result > 0) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
}









