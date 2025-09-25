package com.kh.coreflow.notice.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeDetail;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeInsert;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeResponse;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeSearch;
import com.kh.coreflow.notice.model.service.NoticeService;
import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/notice")
public class NoticeController {
	
	private final NoticeService service;
	private final FileService fileService;
	
	// 공지 조회 + 검색(제목, 내용, 작성자) 
	@GetMapping("/main")
	public ResponseEntity<List<NoticeResponse>> notice(
			@AuthenticationPrincipal UserDeptPoscode auth,
			@ModelAttribute NoticeSearch noticeSearch
			){
		List<NoticeResponse> notiList;
		
		long depId = auth.getDepId();
		long posId = auth.getPosId();
		
		String searchType = noticeSearch.getSearchType();
		String keyword = noticeSearch.getKeyword();
		
		Map<String, Object> params = new HashMap<>();
		params.put("depId", depId);
		params.put("posId", posId);
		
		if(keyword != null && !keyword.trim().isEmpty()) {
			params.put("searchType", searchType);
			params.put("keyword", keyword);
		}
		
		notiList = service.notiList(params);
				
		if(notiList != null && !notiList.isEmpty()) {
			return ResponseEntity.ok(notiList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}

	// 공지 등록(첨부파일)
	@PostMapping("/insert")
	public ResponseEntity<Void> noticeInsert(
			@AuthenticationPrincipal UserDeptPoscode auth,
			@ModelAttribute NoticeInsert insertParams,
			@RequestParam(value = "files", required=false) List<MultipartFile> files
			){
		insertParams.setUserNo(auth.getUserNo());
		
		int result = service.notiInsert(insertParams,files);
		
		if(result > 0) {
			return ResponseEntity.created(URI.create("/notice/insert")).build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 공지 상세 조회(첨부파일)
	@GetMapping("/detail/{notiId}")
	public ResponseEntity<NoticeDetail> noticeDetail(
			@PathVariable int notiId
			){
		List<customFile> files = fileService.getFiles("N",Long.valueOf(notiId));
		
		NoticeDetail notiDetail = service.notiDetail(notiId);
		notiDetail.setFiles(files);
		log.info("files : {}",files);
		if(notiDetail != null) {
			if(notiDetail.getParentDepId() == null) {
				notiDetail.setParentDepId(notiDetail.getChildDepId());
				notiDetail.setChildDepId(null);
			}
			return ResponseEntity.ok(notiDetail);
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 공지 수정(첨부파일)
	@PatchMapping("/update/{notiId}")
	public ResponseEntity<Void> noticeUpdate(
			@PathVariable long notiId,
			@AuthenticationPrincipal UserDeptPoscode auth,
			@ModelAttribute NoticeInsert insertParams,
			@RequestParam(value = "files", required=false) List<MultipartFile> files
			){
		List<customFile> initFiles = fileService.getFiles("N", notiId);
		insertParams.setInitFile(initFiles);

		insertParams.setUserNo(auth.getUserNo());
		insertParams.setNotiId(notiId);
		
		int result = service.notiUpdate(insertParams,files);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 공지 삭제
	@DeleteMapping("/detail/{notiId}")
	public ResponseEntity<Void> noticeDelete(
			@PathVariable int notiId,
			@AuthenticationPrincipal UserDeptPoscode auth
			){
		long userNo = auth.getUserNo();
		Map<String,Object> params = new HashMap<>();
		params.put("notiId", notiId);
		params.put("userNo", userNo);
		
		int result = service.notiDelete(params);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
}